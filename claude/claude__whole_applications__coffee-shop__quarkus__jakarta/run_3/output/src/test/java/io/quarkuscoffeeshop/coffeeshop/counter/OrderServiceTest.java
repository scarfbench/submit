package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.ItemStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.OrderStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderService domain logic.
 * Integration tests run via smoke-tests.sh against the Docker container.
 */
public class OrderServiceTest {

    Logger logger = LoggerFactory.getLogger(OrderServiceTest.class);

    @Test
    public void testOrderCreation() {
        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);
        assertNotNull(orderEventResult);
        assertNotNull(orderEventResult.getOrder());
        assertEquals(OrderStatus.IN_PROGRESS, orderEventResult.getOrder().getOrderStatus());
        assertTrue(orderEventResult.getOrder().getBaristaLineItems().isPresent());
        assertEquals(1, orderEventResult.getOrder().getBaristaLineItems().get().size());
    }

    @Test
    public void testOrderUp() {
        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);
        Order order = orderEventResult.getOrder();

        // create the OrderUp value object
        OrderIn orderIn = orderEventResult.getBaristaTickets().get().get(0);
        OrderUp orderUp = new OrderUp(
                orderIn.orderId,
                orderIn.itemId,
                orderIn.item,
                orderIn.name,
                Instant.now(),
                "Igor");

        OrderEventResult applyResult = order.apply(orderUp);
        assertNotNull(applyResult);
        assertEquals(OrderStatus.FULFILLED, order.getOrderStatus());
        assertEquals(ItemStatus.FULFILLED, order.getBaristaLineItems().get().get(0).getItemStatus());
    }

    @Test
    public void testOrderUpSerialization() {
        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);
        OrderIn orderIn = orderEventResult.getBaristaTickets().get().get(0);
        OrderUp orderUp = new OrderUp(
                orderIn.orderId,
                orderIn.itemId,
                orderIn.item,
                orderIn.name,
                Instant.now(),
                "Igor");

        String json = JsonUtil.toJson(orderUp);
        assertNotNull(json);
        logger.info("OrderUp JSON: {}", json);

        OrderUp deserialized = JsonUtil.fromJsonToOrderUp(json);
        assertNotNull(deserialized);
        assertEquals(orderUp.orderId, deserialized.orderId);
        assertEquals(orderUp.madeBy, deserialized.madeBy);
    }
}
