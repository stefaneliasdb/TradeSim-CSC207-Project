package interface_adapter.order;

import use_case.order.OrderStatus;

/** One immutable snapshot of the order confirmation displayed by the view. */
public record OrderState(OrderStatus status, String message, String orderID, String totalText) {
}
