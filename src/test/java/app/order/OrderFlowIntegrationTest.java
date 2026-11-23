package app.order;

import data_access.order.FileOrderAccountStore;
import data_access.order.OrderAccountAdapter;
import data_access.order.OrderAccountData;
import data_access.order.StockOrderQuoteGateway;
import entity.Order;
import entity.OrderType;
import entity.Stock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.order.OrderOutputData;
import use_case.order.OrderStatus;
import use_case.order.PlaceOrderInputData;
import use_case.order.buy.BuyOrderInteractor;
import use_case.order.sell.SellOrderInteractor;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderFlowIntegrationTest {
    @TempDir
    Path directory;

    @Test
    void buyThenSellUsesCurrentMarketQuoteAndPersistsEntireTradeAcrossReopen() {
        final Path file = directory.resolve("accounts.json");
        final Instant boughtAt = Instant.parse("2026-11-23T15:30:00Z");
        final Instant soldAt = boughtAt.plusSeconds(60);
        final Order prior = new Order("prior-order", "Alice", "MSFT", OrderType.BUY, 2,
                new BigDecimal("5"), boughtAt.minusSeconds(60));
        final FileOrderAccountStore store = new FileOrderAccountStore(file);
        assertTrue(store.create("Alice", new OrderAccountData(new BigDecimal("1000.00"),
                Map.of("MSFT", 2), List.of(prior))));
        final Stock apple = new Stock("AAPL", "Apple", "USD", new BigDecimal("10.125"));
        final StockOrderQuoteGateway quotes = new StockOrderQuoteGateway(symbol ->
                "AAPL".equals(symbol) ? Optional.of(apple) : Optional.empty(), "USD");
        final List<OrderOutputData> results = new ArrayList<>();

        new BuyOrderInteractor(quotes, new OrderAccountAdapter(store), results::add,
                Clock.fixed(boughtAt, ZoneOffset.UTC))
                .execute(new PlaceOrderInputData(" Alice ", " aapl ", 3));

        final FileOrderAccountStore afterBuy = new FileOrderAccountStore(file);
        assertEquals(new BigDecimal("969.625"), afterBuy.find("Alice").orElseThrow().cash());
        assertEquals(Map.of("AAPL", 3, "MSFT", 2), afterBuy.find("Alice").orElseThrow().holdings());
        apple.updatePrice(new BigDecimal("12.225"));
        new SellOrderInteractor(quotes, new OrderAccountAdapter(afterBuy), results::add,
                Clock.fixed(soldAt, ZoneOffset.UTC))
                .execute(new PlaceOrderInputData("Alice", "AAPL", 1));

        final OrderAccountData persisted = new FileOrderAccountStore(file).find("Alice").orElseThrow();
        assertEquals(new BigDecimal("981.850"), persisted.cash());
        assertEquals(Map.of("AAPL", 2, "MSFT", 2), persisted.holdings());
        assertEquals(3, persisted.history().size());
        assertEquals("prior-order", persisted.history().get(0).getOrderID());
        final Order buy = persisted.history().get(1);
        final Order sell = persisted.history().get(2);
        assertEquals(OrderType.BUY, buy.getType());
        assertEquals(3, buy.getQuantity());
        assertEquals(new BigDecimal("10.125"), buy.getExecutionPrice());
        assertEquals(boughtAt, buy.getExecutedAt());
        assertEquals(OrderType.SELL, sell.getType());
        assertEquals(1, sell.getQuantity());
        assertEquals(new BigDecimal("12.225"), sell.getExecutionPrice());
        assertEquals(soldAt, sell.getExecutedAt());
        assertNotEquals(buy.getOrderID(), sell.getOrderID());
        assertEquals(List.of(OrderStatus.FILLED, OrderStatus.FILLED),
                results.stream().map(OrderOutputData::getStatus).toList());
        assertEquals(buy.getOrderID(), results.get(0).getOrderID());
        assertEquals(sell.getOrderID(), results.get(1).getOrderID());
        assertEquals(new BigDecimal("30.375"), results.get(0).getTotalValue());
        assertEquals(new BigDecimal("12.225"), results.get(1).getTotalValue());
    }

    @Test
    void rejectedTradeLeavesPersistedCashHoldingsAndHistoryUntouched() throws Exception {
        final Path file = directory.resolve("accounts.json");
        final FileOrderAccountStore store = new FileOrderAccountStore(file);
        store.create("Alice", new OrderAccountData(new BigDecimal("1.00"), Map.of("MSFT", 2), List.of()));
        final byte[] before = Files.readAllBytes(file);
        final StockOrderQuoteGateway quotes = new StockOrderQuoteGateway(symbol ->
                Optional.of(new Stock(symbol, "Apple", "USD", new BigDecimal("10.125"))), "USD");
        final List<OrderOutputData> results = new ArrayList<>();

        new BuyOrderInteractor(quotes, new OrderAccountAdapter(store), results::add,
                Clock.fixed(Instant.parse("2026-11-23T15:30:00Z"), ZoneOffset.UTC))
                .execute(new PlaceOrderInputData("Alice", "AAPL", 3));

        assertEquals(1, results.size());
        assertEquals(OrderStatus.INSUFFICIENT_CASH, results.getFirst().getStatus());
        assertFalse(results.getFirst().isSuccess());
        assertArrayEquals(before, Files.readAllBytes(file));
        final OrderAccountData reopened = new FileOrderAccountStore(file).find("Alice").orElseThrow();
        assertEquals(new BigDecimal("1.00"), reopened.cash());
        assertEquals(Map.of("MSFT", 2), reopened.holdings());
        assertTrue(reopened.history().isEmpty());
    }
}
