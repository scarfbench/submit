package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BaristaTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaristaTest.class);

    @Test
    public void testMakeBlackCoffee() {
        // Create an OrderIn for a black coffee
        OrderIn orderIn = new OrderIn(UUID.randomUUID().toString(), UUID.randomUUID().toString(), Item.COFFEE_BLACK, "Spock");

        // Verify the OrderIn was created correctly
        assertNotNull(orderIn);
        assertEquals(Item.COFFEE_BLACK, orderIn.item);
        assertEquals("Spock", orderIn.name);
        assertNotNull(orderIn.orderId);
        assertNotNull(orderIn.itemId);

        // Test JSON serialization/deserialization
        String json = JsonUtil.toJson(orderIn);
        assertNotNull(json);
        LOGGER.info("OrderIn JSON: {}", json);

        OrderIn deserialized = JsonUtil.fromJson(json, OrderIn.class);
        assertNotNull(deserialized);
        assertEquals(orderIn.item, deserialized.item);
        assertEquals(orderIn.name, deserialized.name);
    }

}
