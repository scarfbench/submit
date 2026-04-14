package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkuscoffeeshop.coffeeshop.barista.domain.BaristaItem;
import io.quarkuscoffeeshop.coffeeshop.barista.domain.BaristaRepository;
import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.CdiEventBus;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.time.Instant;

import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.*;

/**
 * Inactive Barista implementation - BaristaOutpostImpl is the active one.
 */
public class BaristaImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaristaImpl.class);

    private final String madeBy = "Mr. Spock";

    @Inject
    CdiEventBus eventBus;

    @Inject
    BaristaRepository baristaRepository;

    public void onOrderIn(final String orderInJson) {
        OrderIn orderIn = JsonUtil.fromJson(orderInJson, OrderIn.class);
        BaristaItem baristaItem = new BaristaItem();
        baristaItem.setItem(orderIn.item.toString());
        baristaItem.setTimeIn(Instant.now());
        LOGGER.debug("order in : {}", orderIn);
        try {
            Thread.sleep(calculateDelay(orderIn.item));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        OrderUp orderUp = new OrderUp(
                orderIn.orderId,
                orderIn.itemId,
                orderIn.item,
                orderIn.name,
                Instant.now(),
                madeBy);
        baristaItem.setTimeUp(Instant.now());
        baristaRepository.persist(baristaItem);
        eventBus.publish(ORDERS_UP, JsonUtil.toJson(orderUp));
    }

    private int calculateDelay(final Item item) {
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
