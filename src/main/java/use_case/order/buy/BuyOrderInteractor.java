package use_case.order.buy;

import entity.OrderType;
import use_case.order.OrderAccountGateway;
import use_case.order.OrderQuoteGateway;
import use_case.order.PlaceOrderInputData;

import java.time.Clock;
import java.util.Objects;

/**
 * Scaffold for the buy-shares use case.
 * Dependencies and signatures are ready; trading execution is intentionally unimplemented.
 */
public final class BuyOrderInteractor implements BuyOrderInputBoundary {
    private static final OrderType TYPE = OrderType.BUY;

    private final OrderQuoteGateway quotes;
    private final OrderAccountGateway accounts;
    private final BuyOrderOutputBoundary output;
    private final Clock clock;

    public BuyOrderInteractor(OrderQuoteGateway quotes, OrderAccountGateway accounts,
                              BuyOrderOutputBoundary output, Clock clock) {
        this.quotes = Objects.requireNonNull(quotes);
        this.accounts = Objects.requireNonNull(accounts);
        this.output = Objects.requireNonNull(output);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public void execute(PlaceOrderInputData input) {
        // TODO: Validate and normalize the request, then obtain a coherent account snapshot and quote.
        // TODO: Check sufficient cash and prevent an unsupported holding quantity.
        // TODO: Calculate the updated snapshot and create a candidate Order using TYPE and clock.
        // TODO: Commit cash, this holding and history through accounts.commitTrade as one operation.
        // TODO: Report FILLED only after a successful commit; otherwise report the appropriate result.
        throw new UnsupportedOperationException("TODO: implement the buy-shares use case.");
    }
}

