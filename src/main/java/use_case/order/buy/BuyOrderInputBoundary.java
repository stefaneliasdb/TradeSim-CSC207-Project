package use_case.order.buy;

import use_case.order.PlaceOrderInputData;

/** Entry contract for the buy-shares use case. */
public interface BuyOrderInputBoundary {
    void execute(PlaceOrderInputData input);
}

