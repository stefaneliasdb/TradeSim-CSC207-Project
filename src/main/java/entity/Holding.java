package entity;

import java.math.BigDecimal;
import java.time.Instant;

public record Holding(String symbol, int quantity, BigDecimal totalPurchaseCost,
                      Instant firstPurchasedAt) {
    public Holding {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Holding quantity must be positive.");
        }
    }
}
