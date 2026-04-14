package io.quarkuscoffeeshop.coffeeshop.counter;

import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.ItemStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.OrderStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceOrderUpTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceOrderUpTest.class);

    String orderId;

    OrderEventResult orderEventResult;

    @BeforeEach
    public void setUp() {

        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        orderEventResult = Order.from(placeOrderCommand);
        orderId = orderEventResult.getOrder().getOrderId();
    }

    @Test
    public void testOnOrderUp() {

        // create the OrderUp value object that would be returned by the Barista
        OrderIn orderIn = orderEventResult.getBaristaTickets().get().get(0);
        OrderUp orderUp = new OrderUp(
                orderId,
                orderIn.itemId,
                orderIn.item,
                orderIn.name,
                Instant.now(),
                "Igor");

        // Apply the OrderUp to the order using domain logic (instance method)
        Order order = orderEventResult.getOrder();
        OrderEventResult updatedResult = order.apply(orderUp);

        LOGGER.info("checking updated order");
        Order updatedOrder = updatedResult.getOrder();
        assertNotNull(updatedOrder);
        assertEquals(1, updatedOrder.getBaristaLineItems().get().size());
        assertEquals(ItemStatus.FULFILLED, updatedOrder.getBaristaLineItems().get().get(0).getItemStatus());
        assertEquals(OrderStatus.FULFILLED, updatedOrder.getOrderStatus());
    }
}
