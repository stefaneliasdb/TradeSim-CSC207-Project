package view;

import entity.Order;
import entity.OrderType;
import interface_adapter.order.OrderViewModel;
import interface_adapter.order.buy.BuyOrderController;
import interface_adapter.order.buy.BuyOrderPresenter;
import interface_adapter.order.sell.SellOrderController;
import interface_adapter.order.sell.SellOrderPresenter;
import org.junit.jupiter.api.Test;
import use_case.order.OrderOutputData;
import use_case.order.OrderStatus;
import use_case.order.PlaceOrderInputData;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class OrderViewTest {
    @Test
    void buyRunsOffTheEdtWithTheSessionAccountAndRefreshesOnTheEdt() throws Exception {
        final OrderViewModel model = new OrderViewModel();
        final BuyOrderPresenter presenter = new BuyOrderPresenter(model);
        final AtomicReference<PlaceOrderInputData> request = new AtomicReference<>();
        final AtomicReference<String> refreshedUser = new AtomicReference<>();
        final AtomicBoolean executionOnEdt = new AtomicBoolean(true);
        final AtomicBoolean refreshOnEdt = new AtomicBoolean();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicReference<OrderView> view = new AtomicReference<>();
        onEdt(() -> view.set(new OrderView(new BuyOrderController(input -> {
            executionOnEdt.set(SwingUtilities.isEventDispatchThread());
            request.set(input);
            entered.countDown();
            await(release);
            presenter.present(filled("buy-1", OrderType.BUY));
        }), new SellOrderController(input -> fail("Wrong input boundary")), model, () -> "session-user", user -> {
            refreshOnEdt.set(SwingUtilities.isEventDispatchThread());
            refreshedUser.set(user);
        })));

        try {
            onEdt(() -> {
                view.get().setSymbol("AAPL");
                component(view.get(), "orderQuantity", JTextField.class).setText("2");
                component(view.get(), "buyOrder", JButton.class).doClick();
                assertFalse(component(view.get(), "sellOrder", JButton.class).isEnabled());
            });
            assertTrue(entered.await(3, TimeUnit.SECONDS));
            assertFalse(executionOnEdt.get());
            assertEquals(new PlaceOrderInputData("session-user", "AAPL", 2), request.get());
        }
        finally {
            release.countDown();
        }
        waitUntil(() -> model.getStatus() == OrderStatus.FILLED && refreshedUser.get() != null);
        waitUntil(() -> onEdtBoolean(() -> component(view.get(), "buyOrder", JButton.class).isEnabled()));
        assertEquals("session-user", refreshedUser.get());
        assertTrue(refreshOnEdt.get());
        onEdt(() -> {
            assertEquals("Buy order filled.", component(view.get(), "orderMessage", JTextArea.class).getText());
            assertTrue(component(view.get(), "orderTotal", JLabel.class).getText().contains("$25.00"));
        });
    }

    @Test
    void invalidTextReachesTheSellUseCaseAsAnInvalidQuantityAndDoesNotRefresh() throws Exception {
        final OrderViewModel model = new OrderViewModel();
        final AtomicReference<PlaceOrderInputData> request = new AtomicReference<>();
        final AtomicInteger refreshes = new AtomicInteger();
        final SellOrderPresenter presenter = new SellOrderPresenter(model);
        final AtomicReference<OrderView> view = new AtomicReference<>();
        onEdt(() -> view.set(new OrderView(new BuyOrderController(input -> fail("Wrong input boundary")),
                new SellOrderController(input -> {
                    request.set(input);
                    presenter.present(OrderOutputData.rejected(OrderStatus.INVALID_REQUEST));
                }), model, () -> "signed-in", user -> refreshes.incrementAndGet())));

        onEdt(() -> {
            view.get().setSymbol("AAPL");
            component(view.get(), "orderQuantity", JTextField.class).setText("1.5");
            component(view.get(), "sellOrder", JButton.class).doClick();
        });
        waitUntil(() -> onEdtBoolean(() -> component(view.get(), "sellOrder", JButton.class).isEnabled()));

        assertEquals(new PlaceOrderInputData("signed-in", "AAPL", 0), request.get());
        assertEquals(OrderStatus.INVALID_REQUEST, model.getStatus());
        assertEquals(0, refreshes.get());
        onEdt(() -> assertEquals("", component(view.get(), "orderID", JLabel.class).getText()));
    }

    @Test
    void eachOrderRefreshesOnlyOnceAndARefreshFailureKeepsTheFilledConfirmation() throws Exception {
        final OrderViewModel model = new OrderViewModel();
        final AtomicInteger refreshes = new AtomicInteger();
        final AtomicReference<OrderView> view = new AtomicReference<>();
        onEdt(() -> view.set(new OrderView(new BuyOrderController(input -> { }),
                new SellOrderController(input -> { }), model, () -> "user", user -> {
                    refreshes.incrementAndGet();
                    throw new IllegalStateException("Display unavailable");
                })));
        final BuyOrderPresenter presenter = new BuyOrderPresenter(model);

        presenter.present(filled("first", OrderType.BUY));
        presenter.present(filled("second", OrderType.BUY));
        presenter.present(filled("first", OrderType.BUY));
        onEdt(() -> {
            assertEquals(2, refreshes.get());
            assertEquals(OrderStatus.FILLED, model.getStatus());
            assertEquals("first", model.getOrderID());
            assertEquals("Buy order filled.", component(view.get(), "orderMessage", JTextArea.class).getText());
            assertTrue(component(view.get(), "orderNotice", JTextArea.class).getText().contains("could not refresh"));
        });
    }

    @Test
    void errorAfterPresentationDoesNotTurnACommittedOrderIntoARejection() throws Exception {
        final OrderViewModel model = new OrderViewModel();
        final BuyOrderPresenter presenter = new BuyOrderPresenter(model);
        final AtomicInteger refreshes = new AtomicInteger();
        final AtomicReference<OrderView> view = new AtomicReference<>();
        onEdt(() -> view.set(new OrderView(new BuyOrderController(input -> {
            presenter.present(filled("committed", OrderType.BUY));
            throw new IllegalStateException("Unexpected display error");
        }), new SellOrderController(input -> { }), model, () -> "user", user -> refreshes.incrementAndGet())));

        onEdt(() -> component(view.get(), "buyOrder", JButton.class).doClick());
        waitUntil(() -> onEdtBoolean(() -> component(view.get(), "buyOrder", JButton.class).isEnabled()));

        assertEquals(OrderStatus.FILLED, model.getStatus());
        assertEquals("committed", model.getOrderID());
        assertEquals(1, refreshes.get());
        onEdt(() -> assertTrue(component(view.get(), "orderNotice", JTextArea.class).getText()
                .contains("Check your account before trying again")));
    }

    private static OrderOutputData filled(String id, OrderType type) {
        return OrderOutputData.filled(new Order(id, "user", "AAPL", type, 2,
                new BigDecimal("12.50"), Instant.EPOCH));
    }

    private static <T extends Component> T component(Container root, String name, Class<T> type) {
        for (Component child : root.getComponents()) {
            if (name.equals(child.getName())) {
                return type.cast(child);
            }
            if (child instanceof Container container) {
                final T match = findComponent(container, name, type);
                if (match != null) {
                    return match;
                }
            }
        }
        throw new AssertionError("Missing component " + name);
    }

    private static <T extends Component> T findComponent(Container root, String name, Class<T> type) {
        for (Component child : root.getComponents()) {
            if (name.equals(child.getName())) {
                return type.cast(child);
            }
            if (child instanceof Container container) {
                final T match = findComponent(container, name, type);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    private static void onEdt(Runnable action) throws Exception {
        SwingUtilities.invokeAndWait(action);
    }

    private static boolean onEdtBoolean(BooleanSupplier condition) {
        final AtomicBoolean result = new AtomicBoolean();
        try {
            onEdt(() -> result.set(condition.getAsBoolean()));
        }
        catch (Exception exception) {
            throw new AssertionError(exception);
        }
        return result.get();
    }

    private static void waitUntil(BooleanSupplier condition) throws InterruptedException {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        assertTrue(condition.getAsBoolean(), "Asynchronous order did not finish");
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(3, TimeUnit.SECONDS)) {
                throw new AssertionError("Order was not released");
            }
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }
}
