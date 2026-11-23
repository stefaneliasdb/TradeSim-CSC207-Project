package interface_adapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class StockChartState {

    private final LocalDate date;
    private final BigDecimal closingPrice;

    public StockChartState(
            LocalDate date,
            BigDecimal closingPrice) {

        this.date = Objects.requireNonNull(date);
        this.closingPrice = Objects.requireNonNull(closingPrice);
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getClosingPrice() {
        return closingPrice;
    }
}