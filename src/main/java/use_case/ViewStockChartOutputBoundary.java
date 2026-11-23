package use_case;

import entity.HistoricalPrice;

import java.util.List;

public interface ViewStockChartOutputBoundary {

    void prepareSuccessView(
            String symbol,
            List<HistoricalPrice> prices
    );

    void prepareFailView(String errorMessage);
}