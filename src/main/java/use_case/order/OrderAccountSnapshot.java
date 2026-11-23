package use_case.order;

import java.math.BigDecimal;
import java.util.Objects;

/** Available cash and shares of the requested symbol at the time of the read. */
public record OrderAccountSnapshot(BigDecimal cash, int quantity) {
    public OrderAccountSnapshot {
        Objects.requireNonNull(cash, "Cash is required.");
        if (cash.signum() < 0 || quantity < 0) {
            throw new IllegalArgumentException("Cash and share quantity cannot be negative.");
        }
    }
}
