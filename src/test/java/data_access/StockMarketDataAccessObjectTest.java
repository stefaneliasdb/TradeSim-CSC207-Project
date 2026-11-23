package data_access;

import entity.Stock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StockMarketDataAccessObjectTest {

    @EnabledIfEnvironmentVariable(
            named = "TWELVE_DATA_API_KEY",
            matches = ".+"
    )

    @Test
    void testFindAppleStock() {
        StockMarketDataAccessObject dataAccess =
                new StockMarketDataAccessObject();

        Optional<Stock> result = dataAccess.findStock("AAPL");

        assertTrue(result.isPresent());

        Stock stock = result.get();

        assertEquals("AAPL", stock.getSymbol());
        assertNotNull(stock.getCompanyName());
        assertTrue(stock.getCurrentPrice().signum() > 0);
        assertNotNull(stock.getDailyPercentageChange());

        System.out.println("Symbol: " + stock.getSymbol());
        System.out.println("Company: " + stock.getCompanyName());
        System.out.println("Price: " + stock.getCurrentPrice());
        System.out.println("Daily change: "
                + stock.getDailyPercentageChange() + "%");
    }
}