package interface_adapter.order.sell;

import interface_adapter.order.OrderViewModel;
import interface_adapter.order.OrderResultFormatter;
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
        OrderResultFormatter.present(viewModel, result, "Sell");
    }
}

