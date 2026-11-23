package data_access.order;

import entity.Order;
import entity.OrderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.order.OrderAccountSnapshot;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderAccountAdapterTest {
    @TempDir
    Path directory;

    @Test
    void snapshotContainsCashAndOnlyRequestedSymbolQuantity() {
        final FileOrderAccountStore store = store();
        store.create("Alice", new OrderAccountData(new BigDecimal("100.00"),
                Map.of("AAPL", 3, "MSFT", 7), List.of()));
        final OrderAccountAdapter adapter = new OrderAccountAdapter(store);

        assertEquals(new OrderAccountSnapshot(new BigDecimal("100.00"), 3),
                adapter.findAccount("Alice", "AAPL").orElseThrow());
        assertEquals(new OrderAccountSnapshot(new BigDecimal("100.00"), 7),
                adapter.findAccount("Alice", "MSFT").orElseThrow());
        assertEquals(new OrderAccountSnapshot(new BigDecimal("100.00"), 0),
                adapter.findAccount("Alice", "GOOG").orElseThrow());
        assertTrue(adapter.findAccount("missing", "AAPL").isEmpty());
    }

    @Test
    void commitComparesCashNumericallyAndPreservesUnrelatedHoldingsAndHistory() {
        final FileOrderAccountStore store = store();
        final Order prior = order("prior-order", "MSFT", OrderType.BUY, 7, "1.00");
        store.create("Alice", new OrderAccountData(new BigDecimal("100.00"),
                Map.of("AAPL", 3, "MSFT", 7), List.of(prior)));
        final OrderAccountAdapter adapter = new OrderAccountAdapter(store);
        final Order next = order("next-order", "AAPL", OrderType.BUY, 1, "10.00");

        assertTrue(adapter.commitTrade(next,
                new OrderAccountSnapshot(new BigDecimal("100.0000"), 3),
                new OrderAccountSnapshot(new BigDecimal("90.00"), 4)));

        final OrderAccountData persisted = store().find("Alice").orElseThrow();
        assertEquals(new BigDecimal("90.00"), persisted.cash());
        assertEquals(Map.of("AAPL", 4, "MSFT", 7), persisted.holdings());
        assertEquals(List.of("prior-order", "next-order"),
                persisted.history().stream().map(Order::getOrderID).toList());
        assertEquals(prior.getExecutedAt(), persisted.history().getFirst().getExecutedAt());
        assertEquals(prior.getExecutionPrice(), persisted.history().getFirst().getExecutionPrice());
    }

    @Test
    void staleCashOrQuantityRejectsWithoutWritingAnyState() throws Exception {
        final FileOrderAccountStore store = store();
        store.create("Alice", new OrderAccountData(new BigDecimal("100.00"), Map.of("AAPL", 3), List.of()));
        final byte[] before = Files.readAllBytes(file());
        final OrderAccountAdapter adapter = new OrderAccountAdapter(store);
        final Order next = order("next-order", "AAPL", OrderType.BUY, 1, "10");
        final OrderAccountSnapshot updated = new OrderAccountSnapshot(new BigDecimal("90"), 4);

        assertFalse(adapter.commitTrade(next, new OrderAccountSnapshot(new BigDecimal("101"), 3), updated));
        assertFalse(adapter.commitTrade(next, new OrderAccountSnapshot(new BigDecimal("100"), 2), updated));

        assertArrayEquals(before, Files.readAllBytes(file()));
        assertTrue(store.find("Alice").orElseThrow().history().isEmpty());
    }

    @Test
    void sellingLastSharesRemovesTargetHoldingAndRetainsOthers() {
        final FileOrderAccountStore store = store();
        store.create("Alice", new OrderAccountData(new BigDecimal("100"),
                Map.of("AAPL", 3, "MSFT", 7), List.of()));
        final OrderAccountAdapter adapter = new OrderAccountAdapter(store);
        final Order sale = order("sale", "AAPL", OrderType.SELL, 3, "10");

        assertTrue(adapter.commitTrade(sale, new OrderAccountSnapshot(new BigDecimal("100"), 3),
                new OrderAccountSnapshot(new BigDecimal("130"), 0)));

        final OrderAccountData persisted = store().find("Alice").orElseThrow();
        assertEquals(Map.of("MSFT", 7), persisted.holdings());
        assertEquals(new BigDecimal("130"), persisted.cash());
        assertEquals("sale", persisted.history().getFirst().getOrderID());
    }

    @Test
    void missingAccountCannotBeCreatedByCommit() {
        final OrderAccountAdapter adapter = new OrderAccountAdapter(store());

        assertFalse(adapter.commitTrade(order("buy", "AAPL", OrderType.BUY, 1, "10"),
                new OrderAccountSnapshot(new BigDecimal("100"), 0),
                new OrderAccountSnapshot(new BigDecimal("90"), 1)));

        assertTrue(store().find("Alice").isEmpty());
    }

    @Test
    void duplicateOrderIDFailsWithoutChangingBalanceHoldingsOrHistory() throws Exception {
        final FileOrderAccountStore store = store();
        final Order prior = order("same-id", "AAPL", OrderType.BUY, 3, "10");
        store.create("Alice", new OrderAccountData(new BigDecimal("100"), Map.of("AAPL", 3), List.of(prior)));
        final byte[] before = Files.readAllBytes(file());
        final OrderAccountAdapter adapter = new OrderAccountAdapter(store);

        assertThrows(IllegalArgumentException.class, () -> adapter.commitTrade(
                order("same-id", "AAPL", OrderType.SELL, 1, "10"),
                new OrderAccountSnapshot(new BigDecimal("100"), 3),
                new OrderAccountSnapshot(new BigDecimal("110"), 2)));

        assertArrayEquals(before, Files.readAllBytes(file()));
    }

    @Test
    void simultaneousDifferentSymbolPurchasesCannotSpendSameCashTwice() throws Exception {
        store().create("Alice", new OrderAccountData(new BigDecimal("100"), Map.of(), List.of()));
        final OrderAccountAdapter first = new OrderAccountAdapter(store());
        final OrderAccountAdapter second = new OrderAccountAdapter(store());
        final OrderAccountSnapshot firstRead = first.findAccount("Alice", "AAPL").orElseThrow();
        final OrderAccountSnapshot secondRead = second.findAccount("Alice", "MSFT").orElseThrow();
        final CyclicBarrier ready = new CyclicBarrier(2);
        final var workers = Executors.newFixedThreadPool(2);
        try {
            final var apple = workers.submit(() -> {
                ready.await(10, TimeUnit.SECONDS);
                return first.commitTrade(order("apple", "AAPL", OrderType.BUY, 1, "80"),
                        firstRead, new OrderAccountSnapshot(new BigDecimal("20"), 1));
            });
            final var microsoft = workers.submit(() -> {
                ready.await(10, TimeUnit.SECONDS);
                return second.commitTrade(order("microsoft", "MSFT", OrderType.BUY, 1, "80"),
                        secondRead, new OrderAccountSnapshot(new BigDecimal("20"), 1));
            });

            final boolean appleCommitted = apple.get(20, TimeUnit.SECONDS);
            final boolean microsoftCommitted = microsoft.get(20, TimeUnit.SECONDS);
            assertTrue(appleCommitted ^ microsoftCommitted, "Exactly one stale-snapshot purchase can commit.");
            final OrderAccountData persisted = store().find("Alice").orElseThrow();
            assertEquals(new BigDecimal("20"), persisted.cash());
            assertEquals(1, persisted.history().size());
            assertEquals(Map.of(appleCommitted ? "AAPL" : "MSFT", 1), persisted.holdings());
        }
        finally {
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void accountSnapshotCopiesCollectionsAndCannotBeMutatedByCallers() {
        final Map<String, Integer> holdings = new HashMap<>(Map.of("AAPL", 3));
        final List<Order> history = new ArrayList<>();
        final OrderAccountData data = new OrderAccountData(new BigDecimal("100"), holdings, history);
        holdings.put("AAPL", 999);
        history.add(order("later", "AAPL", OrderType.BUY, 1, "10"));

        assertEquals(Map.of("AAPL", 3), data.holdings());
        assertTrue(data.history().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> data.holdings().put("MSFT", 1));
        assertThrows(UnsupportedOperationException.class, () -> data.history().add(history.getFirst()));
        store().create("Alice", data);
        final OrderAccountData restored = store().find("Alice").orElseThrow();
        assertThrows(UnsupportedOperationException.class, () -> restored.holdings().clear());
        assertThrows(UnsupportedOperationException.class, () -> restored.history().add(history.getFirst()));
    }

    private Path file() {
        return directory.resolve("accounts.json");
    }

    private FileOrderAccountStore store() {
        return new FileOrderAccountStore(file());
    }

    private static Order order(String id, String symbol, OrderType type, int quantity, String price) {
        return new Order(id, "Alice", symbol, type, quantity, new BigDecimal(price),
                Instant.parse("2026-11-23T15:30:00Z"));
    }
}
