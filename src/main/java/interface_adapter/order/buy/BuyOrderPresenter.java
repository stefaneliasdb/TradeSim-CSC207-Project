package interface_adapter.order.buy;

import interface_adapter.order.OrderViewModel;
import interface_adapter.order.OrderResultFormatter;
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
        OrderResultFormatter.present(viewModel, result, "Buy");
    }
}

