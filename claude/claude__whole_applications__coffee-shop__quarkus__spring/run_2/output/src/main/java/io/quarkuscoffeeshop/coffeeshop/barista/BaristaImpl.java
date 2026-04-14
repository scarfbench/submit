package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkuscoffeeshop.coffeeshop.barista.domain.BaristaItem;
import io.quarkuscoffeeshop.coffeeshop.barista.domain.BaristaRepository;
import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class BaristaImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaristaImpl.class);

    private final String madeBy = "Mr. Spock";

    private BaristaRepository baristaRepository;

    public void onOrderIn(final String message) {
        OrderIn orderIn = JsonUtil.fromJson(message, OrderIn.class);
        BaristaItem baristaItem = new BaristaItem();
        baristaItem.setItem(orderIn.item.toString());
        baristaItem.setTimeIn(Instant.now());
        LOGGER.debug("order in : {}", orderIn);
        try {
            Thread.sleep(calculateDelay(orderIn.item));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        OrderUp orderUp = new OrderUp(
                orderIn.orderId,
                orderIn.itemId,
                orderIn.item,
                orderIn.name,
                Instant.now(),
                madeBy);
        baristaItem.setTimeUp(Instant.now());
        baristaRepository.save(baristaItem);
        // Note: Would need event bus to publish ORDERS_UP
        LOGGER.debug("OrderUp: {}", JsonUtil.toJson(orderUp));
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
