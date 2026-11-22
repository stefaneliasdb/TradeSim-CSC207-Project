package interface_adapter.order;

import use_case.order.OrderStatus;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

/** Observable display state shared by the buy and sell presenters. */
public final class OrderViewModel {
    private final PropertyChangeSupport support;
    private OrderStatus status;
    private String message = "";
    private String orderID;
    private String totalText = "";

    public OrderViewModel() {
        support = new PropertyChangeSupport(this);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getTotalText() {
        return totalText;
    }

    public void update(OrderStatus status, String message, String orderID, String totalText) {
        // TODO: Store the display fields together, then fire one property-change notification.
        // Initial status is null until a result exists; rejection must clear stale success values.
        throw new UnsupportedOperationException("TODO: publish the order display state.");
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(Objects.requireNonNull(listener));
    }
}

