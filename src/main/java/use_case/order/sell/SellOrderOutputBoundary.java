package use_case.order.sell;

import use_case.order.OrderOutputData;

/** Result contract for the sell-shares use case; contains no UI formatting. */
public interface SellOrderOutputBoundary {
    void present(OrderOutputData result);
}

