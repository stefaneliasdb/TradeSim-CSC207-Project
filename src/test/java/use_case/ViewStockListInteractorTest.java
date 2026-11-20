package use_case;

import entity.Stock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ViewStockListInteractorTest {

    // Fake data access implementation
    private static class FakeStockDataAccess
            implements ViewStockListDataAccessInterface {

        private final Map<String, Stock> stocks = new HashMap<>();

        public void addStock(Stock stock) {
            stocks.put(stock.getSymbol(), stock);
        }

        @Override
        public Optional<Stock> findStock(String symbol) {
            return Optional.ofNullable(stocks.get(symbol));
        }
    }

    // Fake presenter that records the result
    private static class FakePresenter
            implements ViewStockListOutputBoundary {

        private ViewStockListOutputData outputData;
        private String errorMessage;

        @Override
        public void prepareSuccessView(
                ViewStockListOutputData outputData) {
            this.outputData = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    private Stock createStock(String symbol) {
        return new Stock(
                symbol,
                symbol + " Inc.",
                "USD",
                new BigDecimal("100.00")
        );
    }

    @Test
    void testAllStocksFound() {
        FakeStockDataAccess dataAccess = new FakeStockDataAccess();
        FakePresenter presenter = new FakePresenter();

        dataAccess.addStock(createStock("AAPL"));
        dataAccess.addStock(createStock("TSLA"));
        dataAccess.addStock(createStock("NVDA"));
        dataAccess.addStock(createStock("MSFT"));

        ViewStockListInteractor interactor =
                new ViewStockListInteractor(dataAccess, presenter);

        interactor.execute();

        assertNotNull(presenter.outputData);
        assertNull(presenter.errorMessage);
        assertEquals(4, presenter.outputData.getStocks().size());

        assertEquals(
                "AAPL",
                presenter.outputData.getStocks().get(0).getSymbol()
        );
    }

    @Test
    void testOneStockMissing() {
        FakeStockDataAccess dataAccess = new FakeStockDataAccess();
        FakePresenter presenter = new FakePresenter();

        dataAccess.addStock(createStock("AAPL"));
        dataAccess.addStock(createStock("TSLA"));
        dataAccess.addStock(createStock("NVDA"));

        ViewStockListInteractor interactor =
                new ViewStockListInteractor(dataAccess, presenter);

        interactor.execute();

        assertNotNull(presenter.outputData);
        assertNull(presenter.errorMessage);
        assertEquals(3, presenter.outputData.getStocks().size());
    }

    @Test
    void testNoStocksFound() {
        FakeStockDataAccess dataAccess = new FakeStockDataAccess();
        FakePresenter presenter = new FakePresenter();

        ViewStockListInteractor interactor =
                new ViewStockListInteractor(dataAccess, presenter);

        interactor.execute();

        assertNull(presenter.outputData);
        assertEquals(
                "Unable to retrieve stock information.",
                presenter.errorMessage
        );
    }
}
