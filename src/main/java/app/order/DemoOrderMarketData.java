package app.order;

import entity.HistoricalPrice;
import entity.Stock;
import use_case.ViewStockChartDataAccessInterface;
import use_case.ViewStockListDataAccessInterface;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Explicit offline fixtures for the Order demo; these are never advertised as live quotes. */
final class DemoOrderMarketData implements ViewStockListDataAccessInterface, ViewStockChartDataAccessInterface {
    private final Map<String, Stock> stocks = Map.of(
            "AAPL", new Stock("AAPL", "Apple", "USD", new BigDecimal("190.25")),
            "MSFT", new Stock("MSFT", "Microsoft", "USD", new BigDecimal("420.50")),
            "NVDA", new Stock("NVDA", "NVIDIA", "USD", new BigDecimal("135.75")),
            "TSLA", new Stock("TSLA", "Tesla", "USD", new BigDecimal("250.00")));

    @Override
    public Optional<Stock> findStock(String symbol) {
        return Optional.ofNullable(stocks.get(symbol));
    }

    @Override
    public List<HistoricalPrice> getHistoricalPrices(String symbol, int outputSize) {
        Stock stock = stocks.get(symbol);
        if (stock == null) {
            return List.of();
        }
        List<HistoricalPrice> prices = new ArrayList<>();
        for (int index = Math.min(outputSize, 30) - 1; index >= 0; index--) {
            prices.add(new HistoricalPrice(LocalDate.now().minusDays(index),
                    stock.getCurrentPrice().subtract(new BigDecimal("0.25").multiply(BigDecimal.valueOf(index)))));
        }
        return List.copyOf(prices);
    }
}
