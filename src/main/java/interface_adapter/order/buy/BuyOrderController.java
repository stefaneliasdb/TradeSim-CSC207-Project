package interface_adapter.order.buy;

import use_case.order.buy.BuyOrderInputBoundary;
import use_case.order.PlaceOrderInputData;

import java.util.Objects;

/** Converts the buy UI action into its use-case request. */
public final class BuyOrderController {
    private final BuyOrderInputBoundary input;

    public BuyOrderController(BuyOrderInputBoundary input) {
        this.input = Objects.requireNonNull(input);
    }

    public void placeOrder(String userID, String symbol, int quantity) {
        input.execute(new PlaceOrderInputData(userID, symbol, quantity));
    }
}

