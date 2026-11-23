package interface_adapter.order;

import java.util.List;

/** Display-only snapshot for the standalone Order practice account. */
public record OrderActivityState(String cashText, String holdingsText, List<String> trades) {
    public OrderActivityState {
        trades = List.copyOf(trades);
    }
}
