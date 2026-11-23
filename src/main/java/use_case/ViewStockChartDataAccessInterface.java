package use_case;

import entity.HistoricalPrice;

import java.util.List;

public interface ViewStockChartDataAccessInterface {

    List<HistoricalPrice> getHistoricalPrices(
            String symbol,
            int outputSize
    );
}