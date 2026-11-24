package data_access;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entity.HistoricalPrice;
import entity.Stock;
import io.github.cdimascio.dotenv.Dotenv;
import use_case.ViewStockChartDataAccessInterface;
import use_case.ViewStockListDataAccessInterface;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StockMarketDataAccessObject
        implements ViewStockListDataAccessInterface,
        ViewStockChartDataAccessInterface {

    private static final String QUOTE_URL =
            "https://api.twelvedata.com/quote?symbol=";

    private static final String TIME_SERIES_URL =
            "https://api.twelvedata.com/time_series";

    private static final Duration CACHE_DURATION =
            Duration.ofMinutes(5);

    private final HttpClient client;
    private final String apiKey;

    private final Map<String, CachedPrices> historicalCache =
            new ConcurrentHashMap<>();

    private record CachedPrices(
            List<HistoricalPrice> prices,
            Instant expiresAt) {
    }

    public StockMarketDataAccessObject() {
        this.client = HttpClient.newHttpClient();

        // Load API key from .env or system environment variables.
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        this.apiKey = dotenv.get("TWELVE_DATA_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "TWELVE_DATA_API_KEY is not configured. "
                            + "Add it to your .env file or "
                            + "environment variables.");
        }
    }

    @Override
    public Optional<Stock> findStock(String symbol) {
        String encodedSymbol = URLEncoder.encode(
                symbol, StandardCharsets.UTF_8);

        JsonObject json = sendRequest(
                QUOTE_URL + encodedSymbol);

        String companyName = json.get("name").getAsString();
        String currency = json.get("currency").getAsString();

        BigDecimal price =
                json.get("close").getAsBigDecimal();

        BigDecimal dailyChange =
                json.get("percent_change").getAsBigDecimal();

        return Optional.of(new Stock(
                symbol,
                companyName,
                currency,
                price,
                dailyChange
        ));
    }

    @Override
    public List<HistoricalPrice> getHistoricalPrices(
            String symbol, int outputSize) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Stock symbol cannot be blank");
        }

        if (outputSize <= 0) {
            throw new IllegalArgumentException(
                    "Output size must be positive");
        }

        String cacheKey = symbol.trim().toUpperCase()
                + ":" + outputSize;

        // Reuse cached prices while they remain valid.
        // Concurrent requests for the same key share one fetch.
        CachedPrices cached = historicalCache.compute(
                cacheKey,
                (key, existing) -> {

                    if (existing != null
                            && Instant.now().isBefore(
                            existing.expiresAt())) {
                        return existing;
                    }

                    List<HistoricalPrice> prices =
                            fetchHistoricalPrices(
                                    symbol, outputSize);

                    return new CachedPrices(
                            List.copyOf(prices),
                            Instant.now().plus(CACHE_DURATION)
                    );
                }
        );

        return cached.prices();
    }

    private List<HistoricalPrice> fetchHistoricalPrices(
            String symbol, int outputSize) {

        String encodedSymbol = URLEncoder.encode(
                symbol, StandardCharsets.UTF_8);

        String url = TIME_SERIES_URL
                + "?symbol=" + encodedSymbol
                + "&interval=1day"
                + "&outputsize=" + outputSize;

        JsonObject json = sendRequest(url);

        JsonArray values = json.getAsJsonArray("values");

        if (values == null) {
            throw new IllegalStateException(
                    "No historical prices returned for " + symbol);
        }

        List<HistoricalPrice> prices = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {
            JsonObject item = values.get(i).getAsJsonObject();

            LocalDate date = LocalDate.parse(
                    item.get("datetime").getAsString());

            BigDecimal closingPrice =
                    item.get("close").getAsBigDecimal();

            prices.add(new HistoricalPrice(
                    date, closingPrice));
        }

        // Display prices in chronological order.
        prices.sort(Comparator.comparing(
                HistoricalPrice::getDate));

        return prices;
    }

    private JsonObject sendRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "apikey " + apiKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                throw new IllegalStateException(
                        "Twelve Data rate limit reached. "
                                + "Please try again later.");
            }

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Twelve Data returned HTTP "
                                + response.statusCode());
            }

            JsonObject json = JsonParser.parseString(
                    response.body()).getAsJsonObject();

            if (json.has("status")
                    && "error".equals(
                    json.get("status").getAsString())) {

                String message = json.has("message")
                        ? json.get("message").getAsString()
                        : "Unknown API error";

                throw new IllegalStateException(
                        "Twelve Data error: " + message);
            }

            return json;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to connect to Twelve Data", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Stock request interrupted", e);
        }
    }
}