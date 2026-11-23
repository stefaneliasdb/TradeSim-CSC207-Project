package data_access.order;

import use_case.ViewStockListDataAccessInterface;
import use_case.order.OrderQuoteGateway;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/** Adapts the team's market-data provider to Order's narrower quote port. */
public final class StockOrderQuoteGateway implements OrderQuoteGateway {
    private final ViewStockListDataAccessInterface stocks;
    private final String cashCurrency;

    public StockOrderQuoteGateway(ViewStockListDataAccessInterface stocks, String cashCurrency) {
        this.stocks = Objects.requireNonNull(stocks);
        this.cashCurrency = Objects.requireNonNull(cashCurrency);
    }

    @Override
    public Optional<BigDecimal> findPrice(String symbol) {
        return stocks.findStock(symbol)
                .filter(stock -> symbol.equalsIgnoreCase(stock.getSymbol()))
                .filter(stock -> cashCurrency.equalsIgnoreCase(stock.getCurrency()))
                .map(stock -> stock.getCurrentPrice())
                .filter(price -> price.signum() > 0);
    }
}
