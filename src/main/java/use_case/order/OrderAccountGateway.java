package use_case.order;

import entity.Order;

import java.util.Optional;

/** Account read and commit requirements owned by the Order use cases. */
public interface OrderAccountGateway {
    /**
     * Reads available cash and the requested symbol's quantity as one coherent snapshot.
     * Returns empty if the account does not exist. A missing holding has quantity zero.
     */
    Optional<OrderAccountSnapshot> findAccount(String userID, String symbol);

    /**
     * Compares the current cash and requested holding with the expected snapshot, then
     * commits updated cash, this symbol's quantity and the appended Order as one indivisible
     * operation. Coordinate the entire account across symbols because they share its cash.
     * Preserve unrelated holdings and all earlier history entries.
     *
     * <p>Returns false only if the account disappeared or its expected values no longer match;
     * that result must leave the account and history unchanged. Compare cash numerically,
     * regardless of BigDecimal scale. True means all required changes were committed.
     *
     * <p>The storage implementation must enforce this contract. If lookups expose a live
     * mutable User, do not partially mutate it before a safe commit: use staged state and
     * transaction/atomic replacement support. Separate setters followed by save are not proof
     * of atomicity. Technical failures require separate error handling and rollback; they must
     * not be silently reported as a normal false/conflict or as a successful trade.
     */
    boolean commitTrade(Order order, OrderAccountSnapshot expected, OrderAccountSnapshot updated);
}
