package entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class HistoricalPrice {

    private final LocalDate date;
    private final BigDecimal closingPrice;

    public HistoricalPrice(LocalDate date, BigDecimal closingPrice) {
        this.date = Objects.requireNonNull(date);
        this.closingPrice = Objects.requireNonNull(closingPrice);

        if (closingPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Closing price cannot be negative");
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getClosingPrice() {
        return closingPrice;
    }
}