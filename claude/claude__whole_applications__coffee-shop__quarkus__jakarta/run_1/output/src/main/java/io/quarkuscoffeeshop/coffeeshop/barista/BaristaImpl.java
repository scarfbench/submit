package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Legacy BaristaImpl - functionality now merged into BaristaOutpostImpl.
 * Kept for reference only.
 */
public class BaristaImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaristaImpl.class);

    static int calculateDelay(final Item item) {
        switch (item) {
            case COFFEE_BLACK:
                return 5000;
            case COFFEE_WITH_ROOM:
                return 5000;
            case ESPRESSO:
                return 7000;
            case ESPRESSO_DOUBLE:
                return 7000;
            case CAPPUCCINO:
                return 10000;
            default:
                return 3000;
        }
    }
}
