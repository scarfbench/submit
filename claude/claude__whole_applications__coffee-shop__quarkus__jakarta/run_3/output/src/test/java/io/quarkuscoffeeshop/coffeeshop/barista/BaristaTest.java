package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static io.quarkuscoffeeshop.utils.JsonUtil.fromJsonToOrderUp;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Barista functionality.
 * Integration tests run via smoke-tests.sh against the Docker container.
 */
public class BaristaTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaristaTest.class);

    @Test
    public void testOrderInSerialization() {
        OrderIn orderIn = new OrderIn(UUID.randomUUID().toString(), UUID.randomUUID().toString(), Item.COFFEE_BLACK, "Spock");
        String json = JsonUtil.toJson(orderIn);
        LOGGER.info("OrderIn JSON: {}", json);
        assertNotNull(json);
        assertTrue(json.contains("COFFEE_BLACK"));
    }

    @Test
    public void testOrderUpDeserialization() {
        OrderIn orderIn = new OrderIn(UUID.randomUUID().toString(), UUID.randomUUID().toString(), Item.ESPRESSO, "Kirk");
        String json = JsonUtil.toJson(orderIn);
        OrderIn deserialized = JsonUtil.fromJsonToOrderIn(json);
        assertNotNull(deserialized);
        assertEquals(Item.ESPRESSO, deserialized.item);
        assertEquals("Kirk", deserialized.name);
    }
}
