package data_access.order;

import entity.Stock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockOrderQuoteGatewayTest {
    @Test
    void adaptsTeamStockProviderWithoutRoundingPrice() {
        final AtomicReference<String> requested = new AtomicReference<>();
        final BigDecimal price = new BigDecimal("123.45678901234567890");
        final StockOrderQuoteGateway gateway = new StockOrderQuoteGateway(symbol -> {
            requested.set(symbol);
            return Optional.of(new Stock("aapl", "Apple", "usd", price));
        }, "USD");

        assertEquals(Optional.of(price), gateway.findPrice("AAPL"));
        assertEquals("AAPL", requested.get());
    }

    @Test
    void absentStockProducesNoQuote() {
        final StockOrderQuoteGateway gateway = new StockOrderQuoteGateway(symbol -> Optional.empty(), "USD");

        assertTrue(gateway.findPrice("AAPL").isEmpty());
    }

    @Test
    void foreignCurrencyIsRejectedInsteadOfTreatingItAsDollarCash() {
        final StockOrderQuoteGateway gateway = new StockOrderQuoteGateway(symbol ->
                Optional.of(new Stock("AAPL", "Apple", "CAD", BigDecimal.TEN)), "USD");

        assertTrue(gateway.findPrice("AAPL").isEmpty());
    }

    @Test
    void mismatchedSymbolCannotBeUsedToExecuteRequestedOrder() {
        final StockOrderQuoteGateway gateway = new StockOrderQuoteGateway(symbol ->
                Optional.of(new Stock("MSFT", "Microsoft", "USD", BigDecimal.TEN)), "USD");

        assertTrue(gateway.findPrice("AAPL").isEmpty());
    }

    @Test
    void zeroMarketPriceIsRejected() {
        final StockOrderQuoteGateway gateway = new StockOrderQuoteGateway(symbol ->
                Optional.of(new Stock("AAPL", "Apple", "USD", BigDecimal.ZERO)), "USD");

        assertTrue(gateway.findPrice("AAPL").isEmpty());
    }

    @Test
    void providerFailurePropagatesToUseCaseBoundary() {
        final IllegalStateException failure = new IllegalStateException("Market service offline");
        final StockOrderQuoteGateway gateway = new StockOrderQuoteGateway(symbol -> {
            throw failure;
        }, "USD");

        assertSame(failure, assertThrows(IllegalStateException.class, () -> gateway.findPrice("AAPL")));
    }
}
