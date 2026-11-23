package use_case.order;

import entity.Order;
import entity.OrderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import use_case.order.buy.BuyOrderInteractor;
import use_case.order.sell.SellOrderInteractor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderInteractorsTest {
    private static final Instant EXECUTED_AT = Instant.parse("2026-11-23T15:30:00Z");
    private static final Clock CLOCK = Clock.fixed(EXECUTED_AT, ZoneOffset.UTC);
    private static final PlaceOrderInputData REQUEST = new PlaceOrderInputData("Alice", "AAPL", 3);

    private final AccountGateway accounts = new AccountGateway();
    private final QuoteGateway quotes = new QuoteGateway();
    private final List<OrderOutputData> results = new ArrayList<>();
    private Consumer<OrderOutputData> presenter = results::add;

    @ParameterizedTest
    @EnumSource(Direction.class)
    void successfulTradeCommitsExactStateAndExecutedOrderBeforePresentation(Direction direction) {
        final OrderAccountSnapshot before = accounts.current;
        presenter = result -> {
            assertEquals(1, accounts.history.size(), "State must be committed before FILLED is presented.");
            assertTrue(result.isSuccess());
            results.add(result);
        };

        execute(direction, new PlaceOrderInputData(" Alice ", " aapl ", 3));

        assertEquals("Alice", accounts.userID);
        assertEquals("AAPL", accounts.symbol);
        assertEquals("AAPL", quotes.symbol);
        assertEquals(1, accounts.lookups);
        assertEquals(1, quotes.lookups);
        assertEquals(1, accounts.commits);
        assertSame(before, accounts.expected);
        assertEquals(direction == Direction.BUY ? new BigDecimal("69.625") : new BigDecimal("130.375"),
                accounts.current.cash());
        assertEquals(direction == Direction.BUY ? 8 : 2, accounts.current.quantity());

        final Order order = accounts.history.getFirst();
        assertNotNull(UUID.fromString(order.getOrderID()));
        assertEquals("Alice", order.getUserID());
        assertEquals("AAPL", order.getSymbol());
        assertEquals(direction == Direction.BUY ? OrderType.BUY : OrderType.SELL, order.getType());
        assertEquals(3, order.getQuantity());
        assertEquals(new BigDecimal("10.125"), order.getExecutionPrice());
        assertEquals(EXECUTED_AT, order.getExecutedAt());
        assertEquals(new BigDecimal("30.375"), order.getTotalValue());
        assertEquals(1, results.size());
        assertEquals(OrderStatus.FILLED, results.getFirst().getStatus());
        assertEquals(order.getOrderID(), results.getFirst().getOrderID());
        assertEquals(order.getTotalValue(), results.getFirst().getTotalValue());
    }

    @Test
    void buyAcceptsExactBalanceWithoutRoundingFractionalPrices() {
        accounts.current = new OrderAccountSnapshot(new BigDecimal("0.37037036703703703670"), 0);
        quotes.price = Optional.of(new BigDecimal("0.12345678901234567890"));

        execute(Direction.BUY, REQUEST);

        assertTrue(results.getFirst().isSuccess());
        assertEquals(0, accounts.current.cash().signum());
        assertEquals(3, accounts.current.quantity());
        assertEquals(new BigDecimal("0.37037036703703703670"), results.getFirst().getTotalValue());
    }

    @Test
    void sellAllowsAllOwnedSharesAndPreservesMoneyPrecision() {
        accounts.current = new OrderAccountSnapshot(new BigDecimal("0.01"), 3);
        quotes.price = Optional.of(new BigDecimal("0.12345678901234567890"));

        execute(Direction.SELL, REQUEST);

        assertTrue(results.getFirst().isSuccess());
        assertEquals(new BigDecimal("0.38037036703703703670"), accounts.current.cash());
        assertEquals(0, accounts.current.quantity());
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void invalidRequestDoesNotReadOrMutateAccount(Direction direction, PlaceOrderInputData request) {
        final OrderAccountSnapshot before = accounts.current;

        execute(direction, request);

        assertRejected(OrderStatus.INVALID_REQUEST, before);
        assertEquals(0, accounts.lookups);
        assertEquals(0, quotes.lookups);
        assertEquals(0, accounts.commits);
    }

    static Stream<Arguments> invalidRequests() {
        final List<PlaceOrderInputData> requests = new ArrayList<>();
        requests.add(null);
        requests.add(new PlaceOrderInputData(null, "AAPL", 1));
        requests.add(new PlaceOrderInputData("", "AAPL", 1));
        requests.add(new PlaceOrderInputData(" \t\n", "AAPL", 1));
        requests.add(new PlaceOrderInputData("Alice", null, 1));
        requests.add(new PlaceOrderInputData("Alice", "", 1));
        requests.add(new PlaceOrderInputData("Alice", " \t\n", 1));
        requests.add(new PlaceOrderInputData("Alice", "AAPL", 0));
        requests.add(new PlaceOrderInputData("Alice", "AAPL", -1));
        requests.add(new PlaceOrderInputData("Alice", "AAPL", Integer.MIN_VALUE));
        return Stream.of(Direction.values()).flatMap(direction ->
                requests.stream().map(request -> Arguments.of(direction, request)));
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void symbolNormalizationDoesNotDependOnDefaultLocale(Direction direction) {
        final Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            execute(direction, new PlaceOrderInputData(" Alice ", " ibm\t", 1));
            assertEquals("IBM", accounts.symbol);
            assertEquals("IBM", quotes.symbol);
            assertEquals("Alice", accounts.userID);
        }
        finally {
            Locale.setDefault(previous);
        }
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void missingAccountRejectsWithoutQuoteOrCommit(Direction direction) {
        accounts.current = null;

        execute(direction, REQUEST);

        assertRejected(OrderStatus.ACCOUNT_NOT_FOUND, null);
        assertEquals(0, quotes.lookups);
        assertEquals(0, accounts.commits);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void accountLookupFailureIsServiceUnavailable(Direction direction) {
        final OrderAccountSnapshot before = accounts.current;
        accounts.lookupFailure = new IllegalStateException("Storage offline");

        execute(direction, REQUEST);

        assertRejected(OrderStatus.SERVICE_UNAVAILABLE, before);
        assertEquals(0, quotes.lookups);
        assertEquals(0, accounts.commits);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void invalidAccountGatewayResponseIsServiceUnavailable(Direction direction) {
        final OrderAccountSnapshot before = accounts.current;
        accounts.nullLookup = true;

        execute(direction, REQUEST);

        assertRejected(OrderStatus.SERVICE_UNAVAILABLE, before);
        assertEquals(0, quotes.lookups);
        assertEquals(0, accounts.commits);
    }

    @ParameterizedTest
    @MethodSource("unavailableQuotes")
    void unavailableQuoteRejectsWithoutMutation(Direction direction, Optional<BigDecimal> price) {
        final OrderAccountSnapshot before = accounts.current;
        quotes.price = price;

        execute(direction, REQUEST);

        assertRejected(OrderStatus.QUOTE_UNAVAILABLE, before);
        assertEquals(1, quotes.lookups);
        assertEquals(0, accounts.commits);
    }

    static Stream<Arguments> unavailableQuotes() {
        final List<Optional<BigDecimal>> prices = new ArrayList<>();
        prices.add(null);
        prices.add(Optional.empty());
        prices.add(Optional.of(BigDecimal.ZERO));
        prices.add(Optional.of(new BigDecimal("-0.01")));
        return Stream.of(Direction.values()).flatMap(direction ->
                prices.stream().map(price -> Arguments.of(direction, price)));
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void quoteGatewayFailureIsQuoteUnavailable(Direction direction) {
        final OrderAccountSnapshot before = accounts.current;
        quotes.failure = new IllegalStateException("Quote source offline");

        execute(direction, REQUEST);

        assertRejected(OrderStatus.QUOTE_UNAVAILABLE, before);
        assertEquals(0, accounts.commits);
    }

    @Test
    void buyRejectsInsufficientCashWithoutMutation() {
        accounts.current = new OrderAccountSnapshot(new BigDecimal("30.37499999999999999999"), 5);
        final OrderAccountSnapshot before = accounts.current;

        execute(Direction.BUY, REQUEST);

        assertRejected(OrderStatus.INSUFFICIENT_CASH, before);
        assertEquals(0, accounts.commits);
    }

    @Test
    void buyRejectsHoldingOverflowBeforeLookingUpQuote() {
        accounts.current = new OrderAccountSnapshot(new BigDecimal("1000"), Integer.MAX_VALUE - 2);
        final OrderAccountSnapshot before = accounts.current;

        execute(Direction.BUY, REQUEST);

        assertRejected(OrderStatus.HOLDING_LIMIT_EXCEEDED, before);
        assertEquals(0, quotes.lookups);
        assertEquals(0, accounts.commits);
    }

    @Test
    void buyAllowsLargestSupportedHolding() {
        accounts.current = new OrderAccountSnapshot(new BigDecimal("1000"), Integer.MAX_VALUE - 3);

        execute(Direction.BUY, REQUEST);

        assertTrue(results.getFirst().isSuccess());
        assertEquals(Integer.MAX_VALUE, accounts.current.quantity());
    }

    @Test
    void sellRejectsInsufficientSharesBeforeLookingUpQuote() {
        accounts.current = new OrderAccountSnapshot(new BigDecimal("100"), 2);
        final OrderAccountSnapshot before = accounts.current;

        execute(Direction.SELL, REQUEST);

        assertRejected(OrderStatus.INSUFFICIENT_SHARES, before);
        assertEquals(0, quotes.lookups);
        assertEquals(0, accounts.commits);
    }

    @Test
    void sellCannotCreateAShortPositionFromAMissingHolding() {
        accounts.current = new OrderAccountSnapshot(new BigDecimal("100"), 0);
        final OrderAccountSnapshot before = accounts.current;

        execute(Direction.SELL, new PlaceOrderInputData("Alice", "AAPL", 1));

        assertRejected(OrderStatus.INSUFFICIENT_SHARES, before);
        assertEquals(0, accounts.commits);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void commitConflictIsNotReportedAsFilled(Direction direction) {
        final OrderAccountSnapshot before = accounts.current;
        accounts.acceptCommit = false;

        execute(direction, REQUEST);

        assertRejected(OrderStatus.ACCOUNT_CHANGED, before);
        assertEquals(1, accounts.commits);
        assertSame(before, accounts.expected);
        assertNotNull(accounts.updated);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void failedAtomicCommitIsServiceUnavailable(Direction direction) {
        final OrderAccountSnapshot before = accounts.current;
        accounts.commitFailure = new IllegalStateException("Atomic storage write failed");

        execute(direction, REQUEST);

        assertRejected(OrderStatus.SERVICE_UNAVAILABLE, before);
        assertEquals(1, accounts.commits);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void presenterFailureAfterCommitPropagatesWithoutASecondResult(Direction direction) {
        final IllegalStateException failure = new IllegalStateException("View closed");
        presenter = result -> {
            results.add(result);
            throw failure;
        };

        assertSame(failure, assertThrows(IllegalStateException.class, () -> execute(direction, REQUEST)));

        assertEquals(1, accounts.commits);
        assertEquals(1, accounts.history.size());
        assertEquals(1, results.size());
        assertEquals(OrderStatus.FILLED, results.getFirst().getStatus());
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void consecutiveTradesHaveDistinctOrderIDs(Direction direction) {
        execute(direction, new PlaceOrderInputData("Alice", "AAPL", 1));
        execute(direction, new PlaceOrderInputData("Alice", "AAPL", 1));

        assertEquals(2, accounts.history.size());
        assertNotEquals(accounts.history.get(0).getOrderID(), accounts.history.get(1).getOrderID());
        assertTrue(results.stream().allMatch(OrderOutputData::isSuccess));
    }

    private void execute(Direction direction, PlaceOrderInputData request) {
        if (direction == Direction.BUY) {
            new BuyOrderInteractor(quotes, accounts, presenter::accept, CLOCK).execute(request);
        }
        else {
            new SellOrderInteractor(quotes, accounts, presenter::accept, CLOCK).execute(request);
        }
    }

    private void assertRejected(OrderStatus status, OrderAccountSnapshot before) {
        assertEquals(1, results.size());
        final OrderOutputData result = results.getFirst();
        assertEquals(status, result.getStatus());
        assertFalse(result.isSuccess());
        assertNull(result.getOrderID());
        assertNull(result.getTotalValue());
        assertSame(before, accounts.current);
        assertTrue(accounts.history.isEmpty());
    }

    private enum Direction {
        BUY, SELL
    }

    /** Simulates the gateway's all-or-nothing contract without any I/O dependencies. */
    private static final class AccountGateway implements OrderAccountGateway {
        private OrderAccountSnapshot current = new OrderAccountSnapshot(new BigDecimal("100.00"), 5);
        private final List<Order> history = new ArrayList<>();
        private int lookups;
        private int commits;
        private String userID;
        private String symbol;
        private OrderAccountSnapshot expected;
        private OrderAccountSnapshot updated;
        private RuntimeException lookupFailure;
        private RuntimeException commitFailure;
        private boolean nullLookup;
        private boolean acceptCommit = true;

        @Override
        public Optional<OrderAccountSnapshot> findAccount(String requestedUserID, String requestedSymbol) {
            lookups++;
            userID = requestedUserID;
            symbol = requestedSymbol;
            if (lookupFailure != null) {
                throw lookupFailure;
            }
            return nullLookup ? null : Optional.ofNullable(current);
        }

        @Override
        public boolean commitTrade(Order order, OrderAccountSnapshot expectedSnapshot,
                                   OrderAccountSnapshot updatedSnapshot) {
            commits++;
            expected = expectedSnapshot;
            updated = updatedSnapshot;
            if (commitFailure != null) {
                throw commitFailure;
            }
            if (!acceptCommit || current == null || current.quantity() != expected.quantity()
                    || current.cash().compareTo(expected.cash()) != 0) {
                return false;
            }
            current = updatedSnapshot;
            history.add(order);
            return true;
        }
    }

    private static final class QuoteGateway implements OrderQuoteGateway {
        private Optional<BigDecimal> price = Optional.of(new BigDecimal("10.125"));
        private RuntimeException failure;
        private int lookups;
        private String symbol;

        @Override
        public Optional<BigDecimal> findPrice(String requestedSymbol) {
            lookups++;
            symbol = requestedSymbol;
            if (failure != null) {
                throw failure;
            }
            return price;
        }
    }
}
