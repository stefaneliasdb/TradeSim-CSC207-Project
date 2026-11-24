package data_access.order;

import entity.Order;
import entity.OrderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileOrderAccountStoreTest {
    @TempDir
    Path directory;

    @Test
    void storedAccountSurvivesReopenWithExactMoneyAndExecutionTime() {
        final Path file = directory.resolve("accounts.json");
        final Instant executedAt = Instant.parse("2026-11-23T15:30:00.123456789Z");
        final Order order = new Order("order-1", "Alice", "AAPL", OrderType.BUY, 3,
                new BigDecimal("0.12345678901234567890"), executedAt);
        final OrderAccountData original = new OrderAccountData(new BigDecimal("999.62962963296296296330"),
                Map.of("AAPL", 3, "MSFT", 7), List.of(order));

        assertTrue(new FileOrderAccountStore(file).create("Alice", original));
        final OrderAccountData restored = new FileOrderAccountStore(file).find("Alice").orElseThrow();

        assertEquals(original.cash(), restored.cash());
        assertEquals(original.holdings(), restored.holdings());
        assertEquals(1, restored.history().size());
        final Order restoredOrder = restored.history().getFirst();
        assertEquals(order.getOrderID(), restoredOrder.getOrderID());
        assertEquals(order.getUserID(), restoredOrder.getUserID());
        assertEquals(order.getSymbol(), restoredOrder.getSymbol());
        assertEquals(order.getType(), restoredOrder.getType());
        assertEquals(order.getQuantity(), restoredOrder.getQuantity());
        assertEquals(order.getExecutionPrice(), restoredOrder.getExecutionPrice());
        assertEquals(order.getTotalValue(), restoredOrder.getTotalValue());
        assertEquals(executedAt, restoredOrder.getExecutedAt());
    }

    @Test
    void createNeverReplacesExistingAccountOrHistory() throws Exception {
        final Path file = directory.resolve("accounts.json");
        final FileOrderAccountStore store = new FileOrderAccountStore(file);
        final Order order = new Order("prior-order", "Alice", "AAPL", OrderType.BUY, 1,
                BigDecimal.ONE, Instant.parse("2026-11-20T00:00:00Z"));
        assertTrue(store.create("Alice", new OrderAccountData(new BigDecimal("99.00"),
                Map.of("AAPL", 1), List.of(order))));
        final byte[] before = Files.readAllBytes(file);

        assertFalse(new FileOrderAccountStore(file).create("Alice",
                new OrderAccountData(new BigDecimal("10000"), Map.of(), List.of())));

        assertArrayEquals(before, Files.readAllBytes(file));
        assertEquals("prior-order", store.find("Alice").orElseThrow().history().getFirst().getOrderID());
    }

    @Test
    void missingAccountDoesNotInvokeChangeOrCreateDataFile() {
        final Path file = directory.resolve("accounts.json");
        final FileOrderAccountStore store = new FileOrderAccountStore(file);

        assertTrue(store.find("missing").isEmpty());
        assertFalse(store.update("missing", account -> {
            throw new AssertionError("No callback is allowed for a missing account.");
        }));
        assertFalse(Files.exists(file));
    }

    @Test
    void callbackExceptionLeavesPersistedAccountUnchanged() throws Exception {
        final Path file = directory.resolve("accounts.json");
        final FileOrderAccountStore store = new FileOrderAccountStore(file);
        store.create("Alice", new OrderAccountData(new BigDecimal("100"), Map.of("AAPL", 3), List.of()));
        final byte[] before = Files.readAllBytes(file);
        final IllegalStateException failure = new IllegalStateException("Cannot build replacement");

        assertSame(failure, assertThrows(IllegalStateException.class, () ->
                store.update("Alice", account -> {
                    throw failure;
                })));

        assertArrayEquals(before, Files.readAllBytes(file));
        assertEquals(new BigDecimal("100"), new FileOrderAccountStore(file).find("Alice").orElseThrow().cash());
    }

    @Test
    void declinedUpdateLeavesFileUnchanged() throws Exception {
        final Path file = directory.resolve("accounts.json");
        final FileOrderAccountStore store = new FileOrderAccountStore(file);
        store.create("Alice", new OrderAccountData(new BigDecimal("100"), Map.of("AAPL", 3), List.of()));
        final byte[] before = Files.readAllBytes(file);

        assertFalse(store.update("Alice", account -> Optional.empty()));

        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void wrongOwnerHistoryCannotBeCreatedOrUpdatedAndRetainsOriginalFile() throws Exception {
        final Path file = directory.resolve("accounts.json");
        final FileOrderAccountStore store = new FileOrderAccountStore(file);
        final OrderAccountData original = new OrderAccountData(new BigDecimal("100.00"),
                Map.of("AAPL", 3), List.of());
        store.create("Alice", original);
        final byte[] before = Files.readAllBytes(file);
        final Order aliceOrder = new Order("alice-order", "Alice", "AAPL", OrderType.BUY, 1,
                BigDecimal.TEN, Instant.parse("2026-11-23T15:30:00Z"));
        final Order bobOrder = new Order("bob-order", "Bob", "AAPL", OrderType.BUY, 1,
                BigDecimal.TEN, Instant.parse("2026-11-23T15:30:00Z"));

        assertThrows(RuntimeException.class, () -> store.create("Bob",
                new OrderAccountData(new BigDecimal("90.00"), Map.of("AAPL", 1), List.of(aliceOrder))));
        assertArrayEquals(before, Files.readAllBytes(file));
        assertThrows(RuntimeException.class, () -> store.update("Alice", account -> Optional.of(
                new OrderAccountData(new BigDecimal("90.00"), Map.of("AAPL", 4), List.of(bobOrder)))));
        assertArrayEquals(before, Files.readAllBytes(file));

        final FileOrderAccountStore reopened = new FileOrderAccountStore(file);
        assertTrue(reopened.find("Bob").isEmpty());
        assertEquals(original, reopened.find("Alice").orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "null", "{\"Alice\":null}",
            "{\"Alice\":{\"cash\":-1,\"holdings\":{},\"history\":[]}}",
            "{\"Alice\":{\"cash\":100,\"holdings\":{\"AAPL\":0},\"history\":[]}}"})
    void malformedDataFailsClosedAndRetainsOriginalFile(String malformed) throws Exception {
        final Path file = directory.resolve("accounts.json");
        Files.writeString(file, malformed);
        final byte[] before = Files.readAllBytes(file);
        final FileOrderAccountStore store = new FileOrderAccountStore(file);

        assertThrows(IllegalStateException.class, () -> store.find("Alice"));
        assertThrows(IllegalStateException.class, () -> store.create("Bob",
                new OrderAccountData(BigDecimal.TEN, Map.of(), List.of())));
        assertThrows(IllegalStateException.class, () -> store.update("Alice", Optional::of));

        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void updatingOneAccountPreservesAnotherAccount() {
        final Path file = directory.resolve("accounts.json");
        final FileOrderAccountStore store = new FileOrderAccountStore(file);
        store.create("Alice", new OrderAccountData(new BigDecimal("100"), Map.of("AAPL", 3), List.of()));
        final OrderAccountData bob = new OrderAccountData(new BigDecimal("250.0001"),
                Map.of("MSFT", 7), List.of());
        store.create("Bob", bob);

        assertTrue(store.update("Alice", account -> Optional.of(
                new OrderAccountData(new BigDecimal("90"), Map.of("AAPL", 4), account.history()))));

        final FileOrderAccountStore reopened = new FileOrderAccountStore(file);
        assertEquals(new BigDecimal("90"), reopened.find("Alice").orElseThrow().cash());
        assertEquals(bob, reopened.find("Bob").orElseThrow());
    }
}
