package entity;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {
    private final String orderID;
    private final String userID;
    private final String symbol;
    private final OrderType type;
    private final int quantity;
    private final BigDecimal executionPrice;
    private final Instant executedAt;

    public Order(String orderID, String userID, String symbol,
                 OrderType type, int quantity,
                 BigDecimal executionPrice, Instant executedAt) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        if (executionPrice == null
                || executionPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Execution price must be greater than zero."
            );
        }

        if (orderID == null || orderID.isBlank()) {
            throw new IllegalArgumentException("Order ID is required.");
        }

        if (userID == null || userID.isBlank()) {
            throw new IllegalArgumentException("User ID is required.");
        }

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Stock symbol is required.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Order type is required.");
        }

        if (executedAt == null) {
            throw new IllegalArgumentException("Execution time is required.");
        }

        this.orderID = orderID;
        this.userID = userID;
        this.symbol = symbol;
        this.type = type;
        this.quantity = quantity;
        this.executionPrice = executionPrice;
        this.executedAt = executedAt;
    }

    public String getOrderID() {
        return orderID;
    }
    public String getUserID() {
        return userID;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getExecutionPrice() {
        return executionPrice;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public BigDecimal getTotalValue() {
        return executionPrice.multiply(BigDecimal.valueOf(quantity));
    }
}


