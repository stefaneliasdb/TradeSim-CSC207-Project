package use_case;

import entity.Stock;
import java.util.List;

public class ViewStockListOutputData {

    private final List<Stock> stocks;

    public ViewStockListOutputData(List<Stock> stocks) {
        this.stocks = List.copyOf(stocks);
    }

    public List<Stock> getStocks() {
        return stocks;
    }
}
