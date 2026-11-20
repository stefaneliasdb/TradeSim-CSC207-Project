package use_case;

import entity.Stock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ViewStockListInteractor implements ViewStockListInputBoundary {

    private static final List<String> STOCK_SYMBOLS =
            List.of("AAPL", "TSLA", "NVDA", "MSFT");

    private final ViewStockListDataAccessInterface stockDataAccess;
    private final ViewStockListOutputBoundary presenter;

    public ViewStockListInteractor(
            ViewStockListDataAccessInterface stockDataAccess,
            ViewStockListOutputBoundary presenter) {

        this.stockDataAccess = stockDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        List<Stock> stocks = new ArrayList<>();

        for (String symbol : STOCK_SYMBOLS) {
            Optional<Stock> stock = stockDataAccess.findStock(symbol);

            stock.ifPresent(stocks::add);
        }

        if (stocks.isEmpty()) {
            presenter.prepareFailView("Unable to retrieve stock information.");
        } else {
            presenter.prepareSuccessView(new ViewStockListOutputData(stocks));
        }
    }
}
