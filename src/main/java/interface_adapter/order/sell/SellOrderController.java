package interface_adapter.order.sell;

import use_case.order.sell.SellOrderInputBoundary;

import java.util.Objects;

/** Converts the sell UI action into its use-case request. */
public final class SellOrderController {
    private final SellOrderInputBoundary input;

    public SellOrderController(SellOrderInputBoundary input) {
        this.input = Objects.requireNonNull(input);
    }

    public void placeOrder(String userID, String symbol, int quantity) {
        // TODO: Create PlaceOrderInputData and delegate to input.execute.
        // The user ID must come from the signed-in session, not an editable account selector.
        throw new UnsupportedOperationException("TODO: connect the sell controller.");
    }
}

