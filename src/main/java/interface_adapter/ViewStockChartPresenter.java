package interface_adapter;

import entity.HistoricalPrice;
import use_case.ViewStockChartOutputBoundary;

import java.util.ArrayList;
import java.util.List;

public class ViewStockChartPresenter
        implements ViewStockChartOutputBoundary {

    private final ViewStockChartViewModel viewModel;

    public ViewStockChartPresenter(
            ViewStockChartViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(
            String symbol,
            List<HistoricalPrice> prices) {

        List<StockChartState> chartStates = new ArrayList<>();

        for (HistoricalPrice price : prices) {
            StockChartState state = new StockChartState(
                    price.getDate(),
                    price.getClosingPrice()
            );

            chartStates.add(state);
        }

        viewModel.setChartData(symbol, chartStates);
    }

    @Override
    public void prepareFailView(String errorMessage) {
        viewModel.setErrorMessage(errorMessage);
    }
}