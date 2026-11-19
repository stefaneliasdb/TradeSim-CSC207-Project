package use_case.order.sell;

import use_case.order.PlaceOrderInputData;

/** Entry contract for the sell-shares use case. */
public interface SellOrderInputBoundary {
    void execute(PlaceOrderInputData input);
}

