package use_case;

import entity.HistoricalPrice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViewStockChartInteractorTest {

    @Test
    void testSuccessfulChartRetrieval() {
        HistoricalPrice price = new HistoricalPrice(
                LocalDate.of(2026, 11, 20),
                new BigDecimal("270.50")
        );

        FakeDataAccess dataAccess =
                new FakeDataAccess(List.of(price));

        TestPresenter presenter = new TestPresenter();

        ViewStockChartInteractor interactor =
                new ViewStockChartInteractor(dataAccess, presenter);

        interactor.execute("AAPL");

        assertEquals("AAPL", presenter.symbol);
        assertEquals(List.of(price), presenter.prices);
        assertNull(presenter.errorMessage);

        assertEquals("AAPL", dataAccess.requestedSymbol);
        assertEquals(30, dataAccess.requestedOutputSize);
    }

    @Test
    void testNoHistoricalPrices() {
        FakeDataAccess dataAccess =
                new FakeDataAccess(List.of());

        TestPresenter presenter = new TestPresenter();

        ViewStockChartInteractor interactor =
                new ViewStockChartInteractor(dataAccess, presenter);

        interactor.execute("AAPL");

        assertEquals(
                "No historical prices available for AAPL",
                presenter.errorMessage
        );
        assertNull(presenter.prices);
    }

    @Test
    void testInvalidSymbol() {
        FakeDataAccess dataAccess =
                new FakeDataAccess(List.of());

        TestPresenter presenter = new TestPresenter();

        ViewStockChartInteractor interactor =
                new ViewStockChartInteractor(dataAccess, presenter);

        interactor.execute(" ");

        assertEquals(
                "Please select a stock",
                presenter.errorMessage
        );
        assertNull(dataAccess.requestedSymbol);
    }

    @Test
    void testApiFailure() {
        FakeDataAccess dataAccess =
                new FakeDataAccess(List.of());

        dataAccess.shouldFail = true;

        TestPresenter presenter = new TestPresenter();

        ViewStockChartInteractor interactor =
                new ViewStockChartInteractor(dataAccess, presenter);

        interactor.execute("AAPL");

        assertEquals(
                "API unavailable",
                presenter.errorMessage
        );
        assertNull(presenter.prices);
    }

    private static class FakeDataAccess
            implements ViewStockChartDataAccessInterface {

        private final List<HistoricalPrice> prices;
        private boolean shouldFail;
        private String requestedSymbol;
        private int requestedOutputSize;

        FakeDataAccess(List<HistoricalPrice> prices) {
            this.prices = prices;
        }

        @Override
        public List<HistoricalPrice> getHistoricalPrices(
                String symbol, int outputSize) {

            requestedSymbol = symbol;
            requestedOutputSize = outputSize;

            if (shouldFail) {
                throw new IllegalStateException("API unavailable");
            }

            return prices;
        }
    }

    private static class TestPresenter
            implements ViewStockChartOutputBoundary {

        private String symbol;
        private List<HistoricalPrice> prices;
        private String errorMessage;

        @Override
        public void prepareSuccessView(
                String symbol, List<HistoricalPrice> prices) {

            this.symbol = symbol;
            this.prices = prices;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}