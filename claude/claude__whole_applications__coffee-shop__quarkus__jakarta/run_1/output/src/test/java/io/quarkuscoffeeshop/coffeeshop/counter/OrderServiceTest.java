package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.ItemStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.OrderStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    Logger logger = LoggerFactory.getLogger(OrderServiceTest.class);

    @Test
    /**
     * Verify that Order.from() creates an order with correct initial state
     */
    public void testOnOrderIn() {

        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);

        assertNotNull(orderEventResult);
        assertNotNull(orderEventResult.getOrder());

        Order order = orderEventResult.getOrder();
        assertNotNull(order);
        assertEquals(ItemStatus.IN_PROGRESS, order.getBaristaLineItems().get().get(0).getItemStatus());
        assertEquals(OrderStatus.IN_PROGRESS, order.getOrderStatus());

        // Verify that barista tickets were generated
        assertTrue(orderEventResult.getBaristaTickets().isPresent());
        assertTrue(orderEventResult.getBaristaTickets().get().size() > 0);
    }

    @Test
    public void testOnOrderUp() {

        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        OrderEventResult orderEventResult = Order.from(placeOrderCommand);

        // create the OrderUp value object that would be returned by the Barista
        OrderIn orderIn = orderEventResult.getBaristaTickets().get().get(0);
        OrderUp orderUp = new OrderUp(
                orderIn.orderId,
                orderIn.itemId,
                orderIn.item,
                orderIn.name,
                Instant.now(),
                "Igor");

        // Apply the OrderUp to the order (instance method)
        Order order = orderEventResult.getOrder();
        OrderEventResult updatedResult = order.apply(orderUp);

        Order updatedOrder = updatedResult.getOrder();
        assertNotNull(updatedOrder);
        assertEquals(1, updatedOrder.getBaristaLineItems().get().size());
        assertEquals(ItemStatus.FULFILLED, updatedOrder.getBaristaLineItems().get().get(0).getItemStatus());
        assertEquals(OrderStatus.FULFILLED, updatedOrder.getOrderStatus());
    }
}
