package use_case.order;

import entity.Order;

import java.math.BigDecimal;
import java.util.Objects;

/** Immutable business result; display messages and formatting belong to presenters. */
public final class OrderOutputData {
    private final OrderStatus status;
    private final String orderID;
    private final BigDecimal totalValue;

    private OrderOutputData(OrderStatus status, String orderID, BigDecimal totalValue) {
        this.status = Objects.requireNonNull(status);
        this.orderID = orderID;
        this.totalValue = totalValue;
    }

    /** Call only after the account gateway successfully commits the trade. */
    public static OrderOutputData filled(Order order) {
        Objects.requireNonNull(order);
        return new OrderOutputData(OrderStatus.FILLED, order.getOrderID(), order.getTotalValue());
    }

    public static OrderOutputData rejected(OrderStatus status) {
        Objects.requireNonNull(status);
        if (status == OrderStatus.FILLED) {
            throw new IllegalArgumentException("A rejected order cannot be filled.");
        }
        return new OrderOutputData(status, null, null);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == OrderStatus.FILLED;
    }

    /** Returns the executed order ID, or null if the order was rejected. */
    public String getOrderID() {
        return orderID;
    }

    /** Returns price times quantity, or null if the order was rejected. */
    public BigDecimal getTotalValue() {
        return totalValue;
    }
}
