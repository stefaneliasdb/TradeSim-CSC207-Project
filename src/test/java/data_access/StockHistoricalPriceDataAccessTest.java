package data_access;

import entity.HistoricalPrice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StockHistoricalPriceDataAccessTest {

    @Test
    @EnabledIfEnvironmentVariable(
            named = "TWELVE_DATA_API_KEY",
            matches = ".+"
    )
    void testGetHistoricalPrices() {

        StockMarketDataAccessObject dataAccess =
                new StockMarketDataAccessObject();

        List<HistoricalPrice> prices =
                dataAccess.getHistoricalPrices("AAPL", 30);

        assertFalse(prices.isEmpty());

        assertTrue(prices.size() <= 30);

        for (HistoricalPrice price : prices) {
            assertNotNull(price.getDate());
            assertTrue(price.getClosingPrice().signum() >= 0);
        }

        for (int i = 1; i < prices.size(); i++) {
            assertFalse(
                    prices.get(i).getDate().isBefore(
                            prices.get(i - 1).getDate()
                    )
            );
        }

        System.out.println("Historical prices retrieved: "
                + prices.size());

        for (HistoricalPrice price : prices) {
            System.out.println(
                    price.getDate() + " : "
                            + price.getClosingPrice()
            );
        }
    }
}