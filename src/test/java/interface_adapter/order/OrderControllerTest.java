package interface_adapter.order;

import interface_adapter.order.buy.BuyOrderController;
import interface_adapter.order.sell.SellOrderController;
import org.junit.jupiter.api.Test;
import use_case.order.PlaceOrderInputData;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderControllerTest {
    @Test
    void buyDelegatesRequestWithoutMovingValidationIntoTheController() {
        final AtomicReference<PlaceOrderInputData> received = new AtomicReference<>();
        final BuyOrderController controller = new BuyOrderController(received::set);

        controller.placeOrder("session-user", " aapl ", -2);

        assertEquals(new PlaceOrderInputData("session-user", " aapl ", -2), received.get());
    }

    @Test
    void sellDelegatesToItsOwnInputBoundary() {
        final AtomicReference<PlaceOrderInputData> received = new AtomicReference<>();
        final SellOrderController controller = new SellOrderController(received::set);

        controller.placeOrder("session-user", "MSFT", 3);

        assertEquals(new PlaceOrderInputData("session-user", "MSFT", 3), received.get());
    }
}
