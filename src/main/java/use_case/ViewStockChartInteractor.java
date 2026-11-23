package use_case;

import entity.HistoricalPrice;

import java.util.List;

public class ViewStockChartInteractor
        implements ViewStockChartInputBoundary {

    private static final int DEFAULT_OUTPUT_SIZE = 30;

    private final ViewStockChartDataAccessInterface dataAccess;
    private final ViewStockChartOutputBoundary presenter;

    public ViewStockChartInteractor(
            ViewStockChartDataAccessInterface dataAccess,
            ViewStockChartOutputBoundary presenter) {

        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(String symbol) {

        if (symbol == null || symbol.isBlank()) {
            presenter.prepareFailView(
                    "Please select a stock");
            return;
        }

        try {
            List<HistoricalPrice> prices =
                    dataAccess.getHistoricalPrices(
                            symbol, DEFAULT_OUTPUT_SIZE);

            if (prices.isEmpty()) {
                presenter.prepareFailView(
                        "No historical prices available for " + symbol);
                return;
            }

            presenter.prepareSuccessView(symbol, prices);

        } catch (IllegalStateException exception) {
            presenter.prepareFailView(exception.getMessage());
        }
    }
}