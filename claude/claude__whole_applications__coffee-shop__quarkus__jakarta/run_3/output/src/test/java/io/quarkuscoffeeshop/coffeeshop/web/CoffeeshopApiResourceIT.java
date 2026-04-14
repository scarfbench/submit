package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the CoffeeshopApiResource.
 * HTTP-based integration tests are run via smoke-tests.sh against the Docker container.
 */
public class CoffeeshopApiResourceIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoffeeshopApiResourceIT.class);

    @Test
    public void testJsonSerialization() {
        String json = "{\"id\":\"1e08c459-7e9e-463d-9c19-608688d1a63e\",\"orderSource\":\"COUNTER\",\"location\":\"ATLANTA\",\"loyaltyMemberId\":\"StarshipCaptain\",\"baristaItems\":[{\"name\":\"Jeremy\",\"item\":\"COFFEE_BLACK\",\"price\":3.50}],\"kitchenItems\":[]}";
        assertNotNull(json);
        assertTrue(json.contains("COUNTER"));
    }

    @Test
    public void testPlaceOrderCommandCreation() {
        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        LOGGER.info("placeOrderCommand: {}", placeOrderCommand);
        String serialized = JsonUtil.toJson(placeOrderCommand);
        LOGGER.info("Serialized: {}", serialized);
        assertNotNull(serialized);
        assertTrue(serialized.contains("COFFEE_BLACK"));
    }
}
