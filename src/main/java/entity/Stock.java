package entity;

import java.math.BigDecimal;

public class Stock {

    private final String symbol;
    private final String companyName;
    private final String currency;
    private BigDecimal currentPrice;

    public Stock(String symbol, String companyName, String currency, BigDecimal currentPrice) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Stock symbol cannot be empty");
        }

        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be empty");
        }

        this.symbol = symbol;
        this.companyName = companyName;
        this.currency = currency;

        updatePrice(currentPrice);
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.signum() < 0) {
            throw new IllegalArgumentException("Stock price cannot be negative or null");
        }

        this.currentPrice = newPrice;
    }
}