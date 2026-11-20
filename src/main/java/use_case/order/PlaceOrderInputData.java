package use_case.order;

/**
 * Data supplied when either buying or selling whole shares.
 * The selected use case determines the direction; callers cannot supply an OrderType.
 * Request validation belongs to the corresponding interactor.
 */
public record PlaceOrderInputData(String userID, String symbol, int quantity) {
}

