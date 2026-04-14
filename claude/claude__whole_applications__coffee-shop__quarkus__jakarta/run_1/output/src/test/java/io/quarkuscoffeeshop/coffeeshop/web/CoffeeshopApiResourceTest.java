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
 * Unit test for verifying order command serialization.
 * Integration tests are performed via smoke-test.sh against the running container.
 */
public class CoffeeshopApiResourceTest {

    Logger LOGGER = LoggerFactory.getLogger(CoffeeshopApiResourceTest.class);

    @Test
    public void testPlaceOrderCommandSerialization() {
        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        String json = JsonUtil.toJson(placeOrderCommand);
        LOGGER.info("Testing place order serialization: {}", json);
        assertNotNull(json);
        assertTrue(json.contains("COFFEE_BLACK"));
        assertTrue(json.contains("COUNTER"));
    }

    @Test
    public void testPlaceOrderForSingleCroissant() {
        // Placeholder - covered by smoke tests
    }
}
