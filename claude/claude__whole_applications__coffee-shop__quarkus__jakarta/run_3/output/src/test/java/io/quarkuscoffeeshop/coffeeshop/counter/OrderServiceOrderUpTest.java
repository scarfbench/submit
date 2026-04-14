package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.ItemStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.OrderStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderUp processing logic.
 * Integration tests run via smoke-tests.sh against the Docker container.
 */
public class OrderServiceOrderUpTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceOrderUpTest.class);

    @Test
    public void testOnOrderUp() {

        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);
        Order order = orderEventResult.getOrder();
        String orderId = order.getOrderId();

        // create the OrderUp value object that would be returned by the Barista
        OrderIn orderIn = orderEventResult.getBaristaTickets().get().get(0);
        OrderUp orderUp = new OrderUp(
                orderId,
                orderIn.itemId,
                orderIn.item,
                orderIn.name,
                Instant.now(),
                "Igor");

        // Apply OrderUp to the order
        OrderEventResult applyResult = order.apply(orderUp);

        assertNotNull(applyResult);
        assertEquals(1, order.getBaristaLineItems().get().size());
        assertEquals(ItemStatus.FULFILLED, order.getBaristaLineItems().get().get(0).getItemStatus());
        assertFalse(order.getKitchenLineItems().isPresent());
        assertEquals(OrderStatus.FULFILLED, order.getOrderStatus());
    }
}
