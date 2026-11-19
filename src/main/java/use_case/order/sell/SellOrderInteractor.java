package use_case.order.sell;

import entity.OrderType;
import use_case.order.OrderAccountGateway;
import use_case.order.OrderQuoteGateway;
import use_case.order.PlaceOrderInputData;

import java.time.Clock;
import java.util.Objects;

/**
 * Scaffold for the sell-shares use case.
 * Dependencies and signatures are ready; trading execution is intentionally unimplemented.
 */
public final class SellOrderInteractor implements SellOrderInputBoundary {
    private static final OrderType TYPE = OrderType.SELL;

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
        // TODO: Validate and normalize the request, then obtain a coherent account snapshot and quote.
        // TODO: Check sufficient existing shares; short selling is outside this MVP.
        // TODO: Calculate the updated snapshot and create a candidate Order using TYPE and clock.
        // TODO: Commit cash, this holding and history through accounts.commitTrade as one operation.
        // TODO: Report FILLED only after a successful commit; otherwise report the appropriate result.
        throw new UnsupportedOperationException("TODO: implement the sell-shares use case.");
    }
}

