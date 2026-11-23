package interface_adapter.order;

import use_case.order.OrderStatus;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

/** Observable display state shared by the buy and sell presenters. */
public final class OrderViewModel {
    private final PropertyChangeSupport support;
    private volatile OrderState state = new OrderState(null, "", null, "");

    public OrderViewModel() {
        support = new PropertyChangeSupport(this);
    }

    public OrderStatus getStatus() {
        return state.status();
    }

    public String getMessage() {
        return state.message();
    }

    public String getOrderID() {
        return state.orderID();
    }

    public String getTotalText() {
        return state.totalText();
    }

    public OrderState getState() {
        return state;
    }

    public void update(OrderStatus status, String message, String orderID, String totalText) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(message);
        final OrderState next = status == OrderStatus.FILLED
                ? new OrderState(status, message, Objects.requireNonNull(orderID),
                        Objects.requireNonNull(totalText))
                : new OrderState(status, message, null, "");
        state = next;
        // A repeated rejection is still a completed request, so notify even if its text is unchanged.
        support.firePropertyChange("state", null, next);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(Objects.requireNonNull(listener));
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}

