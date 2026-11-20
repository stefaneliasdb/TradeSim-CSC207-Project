package interface_adapter;

import entity.Stock;
import use_case.ViewStockListOutputBoundary;
import use_case.ViewStockListOutputData;

import java.util.ArrayList;
import java.util.List;

public class ViewStockListPresenter
        implements ViewStockListOutputBoundary {

    private final ViewStockListViewModel viewModel;

    public ViewStockListPresenter(
            ViewStockListViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(
            ViewStockListOutputData outputData) {

        List<StockListState> stockStates = new ArrayList<>();

        for (Stock stock : outputData.getStocks()) {

            String price = stock.getCurrency() + " "
                    + stock.getCurrentPrice();

            String dailyChange = stock.getDailyPercentageChange()
                    .signum() > 0 ? "+" : "";

            dailyChange += stock.getDailyPercentageChange() + "%";

            StockListState state = new StockListState(
                    stock.getSymbol(),
                    stock.getCompanyName(),
                    price,
                    dailyChange
            );

            stockStates.add(state);
        }

        viewModel.setStocks(stockStates);
        viewModel.setErrorMessage("");
    }

    @Override
    public void prepareFailView(String errorMessage) {
        viewModel.setStocks(List.of());
        viewModel.setErrorMessage(errorMessage);
    }
}