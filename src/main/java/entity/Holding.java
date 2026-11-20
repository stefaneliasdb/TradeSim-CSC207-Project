package entity;

import java.math.BigDecimal;
import java.time.Instant;

public record Holding(String symbol, int quantity, BigDecimal costBasis, Instant firstPurchasedAt) {
}
