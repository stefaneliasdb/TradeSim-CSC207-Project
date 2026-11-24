package interface_adapter.order.sell;

import use_case.order.sell.SellOrderInputBoundary;
import use_case.order.PlaceOrderInputData;

import java.util.Objects;

/** Converts the sell UI action into its use-case request. */
public final class SellOrderController {
    private final SellOrderInputBoundary input;

    public SellOrderController(SellOrderInputBoundary input) {
        this.input = Objects.requireNonNull(input);
    }

    public void placeOrder(String userID, String symbol, int quantity) {
        input.execute(new PlaceOrderInputData(userID, symbol, quantity));
    }
}

