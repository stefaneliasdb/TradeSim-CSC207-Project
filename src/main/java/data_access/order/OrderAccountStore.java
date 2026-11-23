package data_access.order;

import java.util.Optional;
import java.util.function.Function;

/** Storage operations for the local practice account; all writers share one transaction boundary. */
public interface OrderAccountStore {
    Optional<OrderAccountData> find(String userID);

    /** Creates a practice account only if absent. Never replaces an existing balance/history. */
    boolean create(String userID, OrderAccountData initial);

    /**
     * Reloads the account and runs the change while holding the store's write lock.
     * Empty means a conflict with no mutation. Exceptions before commit leave the original intact.
     * A successful return means the entire replacement has been persisted.
     */
    boolean update(String userID, Function<OrderAccountData, Optional<OrderAccountData>> change);
}
