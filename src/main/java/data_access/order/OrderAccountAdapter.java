package data_access.order;

import entity.Order;
import use_case.order.OrderAccountGateway;
import use_case.order.OrderAccountSnapshot;

import java.util.Optional;

/**
 * Account integration scaffold. The team's account DAO is not wired yet.
 *
 * <p>Integrate with UserDataAccessObject and the team's User/Portfolio APIs when
 * those modules are available. Do not create placeholder copies of their classes.
 * If a lookup returns a live mutable User, stage changes rather than mutating it
 * before a safe commit. Independent setters followed by save do not establish
 * an atomic transaction. All trade writers must coordinate at account level.
 */
public final class OrderAccountAdapter implements OrderAccountGateway {
    public OrderAccountAdapter() {
        // TODO: Add constructor injection for the team's actual transaction-capable store.
    }

    @Override
    public Optional<OrderAccountSnapshot> findAccount(String userID, String symbol) {
        // TODO: Read cash and this symbol's quantity coherently through the team's account store.
        throw new UnsupportedOperationException("TODO: integrate the account lookup.");
    }

    @Override
    public boolean commitTrade(Order order, OrderAccountSnapshot expected, OrderAccountSnapshot updated) {
        // TODO: Compare expected state and atomically commit cash, target holding and order history.
        throw new UnsupportedOperationException("TODO: integrate the atomic account commit.");
    }
}

