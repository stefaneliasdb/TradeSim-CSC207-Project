package use_case.order.sell;

import entity.Order;
import entity.OrderType;
import use_case.order.OrderAccountGateway;
import use_case.order.OrderAccountSnapshot;
import use_case.order.OrderOutputData;
import use_case.order.OrderQuoteGateway;
import use_case.order.OrderStatus;
import use_case.order.PlaceOrderInputData;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Validates and executes a sale of owned whole shares; short selling is unsupported. */
public final class SellOrderInteractor implements SellOrderInputBoundary {
    private final OrderQuoteGateway quotes;
    private final OrderAccountGateway accounts;
    private final SellOrderOutputBoundary output;
    private final Clock clock;

    public SellOrderInteractor(OrderQuoteGateway quotes, OrderAccountGateway accounts,
                               SellOrderOutputBoundary output, Clock clock) {
        this.quotes = Objects.requireNonNull(quotes);
        this.accounts = Objects.requireNonNull(accounts);
        this.output = Objects.requireNonNull(output);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public void execute(PlaceOrderInputData input) {
        // A presenter failure cannot turn an already committed sale into a rejection.
        output.present(placeOrder(input));
    }

    private OrderOutputData placeOrder(PlaceOrderInputData input) {
        if (input == null || input.userID() == null || input.userID().isBlank()
                || input.symbol() == null || input.symbol().isBlank() || input.quantity() <= 0) {
            return OrderOutputData.rejected(OrderStatus.INVALID_REQUEST);
        }
        final String userID = input.userID().strip();
        final String symbol = input.symbol().strip().toUpperCase(Locale.ROOT);

        final Optional<OrderAccountSnapshot> account;
        try {
            account = accounts.findAccount(userID, symbol);
        }
        catch (RuntimeException exception) {
            return OrderOutputData.rejected(OrderStatus.SERVICE_UNAVAILABLE);
        }
        if (account == null) {
            return OrderOutputData.rejected(OrderStatus.SERVICE_UNAVAILABLE);
        }
        if (account.isEmpty()) {
            return OrderOutputData.rejected(OrderStatus.ACCOUNT_NOT_FOUND);
        }
        final OrderAccountSnapshot current = account.get();
        if (current.quantity() < input.quantity()) {
            return OrderOutputData.rejected(OrderStatus.INSUFFICIENT_SHARES);
        }

        final Optional<BigDecimal> quote;
        try {
            quote = quotes.findPrice(symbol);
        }
        catch (RuntimeException exception) {
            return OrderOutputData.rejected(OrderStatus.QUOTE_UNAVAILABLE);
        }
        if (quote == null || quote.isEmpty() || quote.get().signum() <= 0) {
            return OrderOutputData.rejected(OrderStatus.QUOTE_UNAVAILABLE);
        }
        final BigDecimal price = quote.get();
        final BigDecimal total = price.multiply(BigDecimal.valueOf(input.quantity()));
        final OrderAccountSnapshot updated = new OrderAccountSnapshot(current.cash().add(total),
                current.quantity() - input.quantity());
        final Order order = new Order(UUID.randomUUID().toString(), userID, symbol, OrderType.SELL,
                input.quantity(), price, clock.instant());
        final boolean committed;
        try {
            committed = accounts.commitTrade(order, current, updated);
        }
        catch (RuntimeException exception) {
            return OrderOutputData.rejected(OrderStatus.SERVICE_UNAVAILABLE);
        }
        return committed ? OrderOutputData.filled(order)
                : OrderOutputData.rejected(OrderStatus.ACCOUNT_CHANGED);
    }
}
