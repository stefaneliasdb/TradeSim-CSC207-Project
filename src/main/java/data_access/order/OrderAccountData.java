package data_access.order;

import entity.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable storage snapshot, not a replacement for the team's User or Portfolio entity. */
public record OrderAccountData(BigDecimal cash, Map<String, Integer> holdings, List<Order> history) {
    public OrderAccountData {
        Objects.requireNonNull(cash);
        holdings = Map.copyOf(holdings);
        history = List.copyOf(history);
        if (cash.signum() < 0 || holdings.entrySet().stream().anyMatch(
                entry -> entry.getKey().isBlank() || entry.getValue() <= 0)) {
            throw new IllegalArgumentException("Invalid stored account balance or holding.");
        }
    }
}
