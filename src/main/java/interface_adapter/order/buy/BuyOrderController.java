package interface_adapter.order.buy;

import use_case.order.buy.BuyOrderInputBoundary;

import java.util.Objects;

/** Converts the buy UI action into its use-case request. */
public final class BuyOrderController {
    private final BuyOrderInputBoundary input;

    public BuyOrderController(BuyOrderInputBoundary input) {
        this.input = Objects.requireNonNull(input);
    }

    public void placeOrder(String userID, String symbol, int quantity) {
        // TODO: Create PlaceOrderInputData and delegate to input.execute.
        // The user ID must come from the signed-in session, not an editable account selector.
        throw new UnsupportedOperationException("TODO: connect the buy controller.");
    }
}

