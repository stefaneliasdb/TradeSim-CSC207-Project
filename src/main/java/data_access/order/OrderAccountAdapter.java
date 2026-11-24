package data_access.order;

import entity.Order;
import use_case.order.OrderAccountGateway;
import use_case.order.OrderAccountSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/** Translates the Order account port to an atomic store operation. */
public final class OrderAccountAdapter implements OrderAccountGateway {
    private final OrderAccountStore store;

    public OrderAccountAdapter(OrderAccountStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public Optional<OrderAccountSnapshot> findAccount(String userID, String symbol) {
        return store.find(userID).map(account -> new OrderAccountSnapshot(
                account.cash(), account.holdings().getOrDefault(symbol, 0)));
    }

    @Override
    public boolean commitTrade(Order order, OrderAccountSnapshot expected,
                               OrderAccountSnapshot updated) {
        Objects.requireNonNull(order);
        Objects.requireNonNull(expected);
        Objects.requireNonNull(updated);
        return store.update(order.getUserID(), current -> {
            int held = current.holdings().getOrDefault(order.getSymbol(), 0);
            if (current.cash().compareTo(expected.cash()) != 0 || held != expected.quantity()) {
                return Optional.empty();
            }
            if (current.history().stream().anyMatch(
                    previous -> previous.getOrderID().equals(order.getOrderID()))) {
                throw new IllegalArgumentException("Order ID has already been recorded.");
            }
            var holdings = new HashMap<>(current.holdings());
            if (updated.quantity() == 0) {
                holdings.remove(order.getSymbol());
            } else {
                holdings.put(order.getSymbol(), updated.quantity());
            }
            var history = new ArrayList<>(current.history());
            history.add(order);
            return Optional.of(new OrderAccountData(updated.cash(), holdings, history));
        });
    }
}

