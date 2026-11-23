package interface_adapter.order;

import entity.Order;
import entity.OrderType;
import interface_adapter.order.buy.BuyOrderPresenter;
import interface_adapter.order.sell.SellOrderPresenter;
import org.junit.jupiter.api.Test;
import use_case.order.OrderOutputData;
import use_case.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderPresenterTest {
    @Test
    void buyFormatsTheExactTradeValueForDisplay() {
        final OrderViewModel model = new OrderViewModel();
        final Order order = new Order("order-1", "user", "AAPL", OrderType.BUY, 3,
                new BigDecimal("0.125"), Instant.EPOCH);
        final List<OrderState> events = new ArrayList<>();
        model.addPropertyChangeListener(event -> events.add((OrderState) event.getNewValue()));

        new BuyOrderPresenter(model).present(OrderOutputData.filled(order));

        assertEquals(OrderStatus.FILLED, model.getStatus());
        assertEquals("Buy order filled.", model.getMessage());
        assertEquals("order-1", model.getOrderID());
        assertEquals("$0.38", model.getTotalText());
        assertEquals(new BigDecimal("0.375"), order.getTotalValue());
        assertEquals(List.of(model.getState()), events);
    }

    @Test
    void sellUsesSellWordingAndEveryRejectionClearsThePreviousConfirmation() {
        final OrderViewModel model = new OrderViewModel();
        final SellOrderPresenter presenter = new SellOrderPresenter(model);
        for (OrderStatus status : OrderStatus.values()) {
            if (status == OrderStatus.FILLED) {
                continue;
            }
            presenter.present(OrderOutputData.filled(new Order("sold", "user", "AAPL", OrderType.SELL,
                    2, new BigDecimal("12.25"), Instant.EPOCH)));
            assertEquals("Sell order filled.", model.getMessage());
            assertEquals("$24.50", model.getTotalText());

            presenter.present(OrderOutputData.rejected(status));

            assertEquals(status, model.getStatus());
            assertTrue(model.getMessage().startsWith("Sell order rejected:"));
            assertNull(model.getOrderID(), status.name());
            assertEquals("", model.getTotalText(), status.name());
        }
    }

    @Test
    void repeatedResultsStillProduceOneCoherentEventPerRequest() {
        final OrderViewModel model = new OrderViewModel();
        final List<OrderState> events = new ArrayList<>();
        model.addPropertyChangeListener(event -> events.add((OrderState) event.getNewValue()));
        final BuyOrderPresenter presenter = new BuyOrderPresenter(model);

        presenter.present(OrderOutputData.rejected(OrderStatus.INSUFFICIENT_CASH));
        presenter.present(OrderOutputData.rejected(OrderStatus.INSUFFICIENT_CASH));

        assertEquals(2, events.size());
        assertEquals(events.get(0), events.get(1));
        assertNull(events.get(1).orderID());
        assertEquals("", events.get(1).totalText());
    }

    @Test
    void modelClearsStaleSuccessValuesEvenIfACallerPassesThemOnRejection() {
        final OrderViewModel model = new OrderViewModel();

        model.update(OrderStatus.INVALID_REQUEST, "Invalid request", "old-order", "$100.00");

        assertEquals(new OrderState(OrderStatus.INVALID_REQUEST, "Invalid request", null, ""), model.getState());
    }
}
