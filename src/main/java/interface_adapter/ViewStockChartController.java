package interface_adapter;

import use_case.ViewStockChartInputBoundary;

public class ViewStockChartController {

    private final ViewStockChartInputBoundary interactor;

    public ViewStockChartController(
            ViewStockChartInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String symbol) {
        interactor.execute(symbol);
    }
}