package use_case.order;

import java.math.BigDecimal;
import java.util.Optional;

public interface OrderQuoteGateway {
    /** Returns a market price in the simulator's cash currency, or empty if unavailable. */
    Optional<BigDecimal> findPrice(String symbol);
}
