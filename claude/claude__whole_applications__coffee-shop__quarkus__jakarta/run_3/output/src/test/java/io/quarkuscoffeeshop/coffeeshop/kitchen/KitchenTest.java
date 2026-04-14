package io.quarkuscoffeeshop.coffeeshop.kitchen;

import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Kitchen functionality.
 * Integration tests run via smoke-tests.sh against the Docker container.
 */
public class KitchenTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KitchenTest.class);

    @Test
    public void testOrderInSerialization() {
        OrderIn orderIn = new OrderIn(UUID.randomUUID().toString(), UUID.randomUUID().toString(), Item.CROISSANT, "Spock");
        String json = JsonUtil.toJson(orderIn);
        LOGGER.info("OrderIn JSON: {}", json);
        assertNotNull(json);
        assertTrue(json.contains("CROISSANT"));
        assertTrue(json.contains("Spock"));
    }

    @Test
    public void testOrderInDeserialization() {
        OrderIn orderIn = new OrderIn(UUID.randomUUID().toString(), UUID.randomUUID().toString(), Item.MUFFIN, "Kirk");
        String json = JsonUtil.toJson(orderIn);
        OrderIn deserialized = JsonUtil.fromJsonToOrderIn(json);
        assertNotNull(deserialized);
        assertEquals(Item.MUFFIN, deserialized.item);
        assertEquals("Kirk", deserialized.name);
    }
}
