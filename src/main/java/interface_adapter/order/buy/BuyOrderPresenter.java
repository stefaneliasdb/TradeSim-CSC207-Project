package interface_adapter.order.buy;

import interface_adapter.order.OrderViewModel;
import use_case.order.OrderOutputData;
import use_case.order.buy.BuyOrderOutputBoundary;

import java.util.Objects;

/** Maps buy-use-case results into display state. */
public final class BuyOrderPresenter implements BuyOrderOutputBoundary {
    private final OrderViewModel viewModel;

    public BuyOrderPresenter(OrderViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel);
    }

    @Override
    public void present(OrderOutputData result) {
        // TODO: Map status to a buy-specific message and format the total for display.
        // TODO: Clear old confirmation values on rejection and update the shared view model.
        throw new UnsupportedOperationException("TODO: implement the buy presenter.");
    }
}

