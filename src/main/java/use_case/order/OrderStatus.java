package use_case.order;

/** Outcome of a simulated market order. */
public enum OrderStatus {
    FILLED,
    INVALID_REQUEST,
    ACCOUNT_NOT_FOUND,
    QUOTE_UNAVAILABLE,
    INSUFFICIENT_CASH,
    INSUFFICIENT_SHARES,
    HOLDING_LIMIT_EXCEEDED,
    ACCOUNT_CHANGED,
    SERVICE_UNAVAILABLE
}
