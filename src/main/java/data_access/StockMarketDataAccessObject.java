package data_access;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entity.Stock;
import use_case.ViewStockListDataAccessInterface;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class StockMarketDataAccessObject
        implements ViewStockListDataAccessInterface {

    private static final String BASE_URL =
            "https://api.twelvedata.com/quote?symbol=";

    private final HttpClient client;
    private final String apiKey;

    public StockMarketDataAccessObject() {
        this.client = HttpClient.newHttpClient();
        this.apiKey = System.getenv("TWELVE_DATA_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "TWELVE_DATA_API_KEY is not configured");
        }
    }

    @Override
    public Optional<Stock> findStock(String symbol) {
        String encodedSymbol = URLEncoder.encode(
                symbol, StandardCharsets.UTF_8);

        String url = BASE_URL + encodedSymbol;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "apikey " + apiKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Twelve Data returned HTTP "
                                + response.statusCode());
            }

            JsonObject json = JsonParser.parseString(
                    response.body()).getAsJsonObject();

            if (json.has("status")
                    && "error".equals(json.get("status").getAsString())) {
                throw new IllegalStateException(
                        "Twelve Data error: "
                                + json.get("message").getAsString());
            }

            String companyName = json.get("name").getAsString();
            String currency = json.get("currency").getAsString();

            BigDecimal price = json.get("close").getAsBigDecimal();
            BigDecimal dailyChange =
                    json.get("percent_change").getAsBigDecimal();

            return Optional.of(new Stock(
                    symbol,
                    companyName,
                    currency,
                    price,
                    dailyChange
            ));

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