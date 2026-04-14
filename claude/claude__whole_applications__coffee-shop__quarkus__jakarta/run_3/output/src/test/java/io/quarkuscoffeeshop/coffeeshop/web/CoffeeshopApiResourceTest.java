package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.utils.TestUtils;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Basic test to verify PlaceOrderCommand serialization.
 * Integration tests are run against the Docker container via smoke-tests.sh.
 */
public class CoffeeshopApiResourceTest {

    Logger LOGGER = LoggerFactory.getLogger(CoffeeshopApiResourceTest.class);

    @Test
    public void testPlaceOrderCommandSerialization() {
        PlaceOrderCommand placeOrderCommand = TestUtils.mockPlaceOrderCommand();
        String json = JsonUtil.toJson(placeOrderCommand);
        LOGGER.info("PlaceOrderCommand JSON: {}", json);
        assertNotNull(json);
        assertNotNull(placeOrderCommand.getId());
    }
}
