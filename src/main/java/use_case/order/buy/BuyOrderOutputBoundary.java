package use_case.order.buy;

import use_case.order.OrderOutputData;

/** Result contract for the buy-shares use case; contains no UI formatting. */
public interface BuyOrderOutputBoundary {
    void present(OrderOutputData result);
}

