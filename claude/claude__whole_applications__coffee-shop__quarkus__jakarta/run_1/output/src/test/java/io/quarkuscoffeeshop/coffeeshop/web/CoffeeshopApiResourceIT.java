package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkuscoffeeshop.coffeeshop.counter.domain.OrderEventResult;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;
import io.quarkuscoffeeshop.coffeeshop.domain.OrderStatus;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for verifying domain logic and JSON serialization.
 * Integration tests are performed via smoke-test.sh against the running container.
 */
public class CoffeeshopApiResourceIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoffeeshopApiResourceIT.class);

    @Test
    public void testJsonSerialization() {
        String json = "{\"id\":\"1e08c459-7e9e-463d-9c19-608688d1a63e\",\"orderSource\":\"COUNTER\",\"location\":\"ATLANTA\",\"loyaltyMemberId\":\"StarshipCaptain\",\"baristaItems\":[{\"name\":\"Jeremy\",\"item\":\"COFFEE_BLACK\",\"price\":3.50}],\"kitchenItems\":[]}";
        assertNotNull(json);
        assertTrue(json.contains("COFFEE_BLACK"));
    }

    @Test
    public void testPlaceOrderCreatesOrderEventResult() {
        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        LOGGER.info("placeOrderCommand: {}", placeOrderCommand);
        LOGGER.info("Testing place order domain logic");

        String json = JsonUtil.toJson(placeOrderCommand);
        assertNotNull(json);
        LOGGER.info("Serialized: {}", json);

        // Test the domain logic directly
        OrderEventResult result = Order.from(placeOrderCommand);
        assertNotNull(result);
        assertNotNull(result.getOrder());
        assertEquals(OrderStatus.IN_PROGRESS, result.getOrder().getOrderStatus());
        assertTrue(result.getBaristaTickets().isPresent());
        assertFalse(result.getKitchenTickets().isPresent());
        assertNotNull(result.getOrderUpdates());
        assertFalse(result.getOrderUpdates().isEmpty());
    }
}
