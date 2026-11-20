package interface_adapter.order.sell;

import interface_adapter.order.OrderViewModel;
import use_case.order.OrderOutputData;
import use_case.order.sell.SellOrderOutputBoundary;

import java.util.Objects;

/** Maps sell-use-case results into display state. */
public final class SellOrderPresenter implements SellOrderOutputBoundary {
    private final OrderViewModel viewModel;

    public SellOrderPresenter(OrderViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel);
    }

    @Override
    public void present(OrderOutputData result) {
        // TODO: Map status to a sell-specific message and format the total for display.
        // TODO: Clear old confirmation values on rejection and update the shared view model.
        throw new UnsupportedOperationException("TODO: implement the sell presenter.");
    }
}

