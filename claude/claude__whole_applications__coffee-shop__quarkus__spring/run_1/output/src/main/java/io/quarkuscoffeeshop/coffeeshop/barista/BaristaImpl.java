package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkuscoffeeshop.coffeeshop.barista.domain.BaristaItem;
import io.quarkuscoffeeshop.coffeeshop.barista.domain.BaristaRepository;
import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusService;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

/**
 * Local barista implementation (not used when Kafka outpost is active)
 */
@Service
public class BaristaImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaristaImpl.class);

    private final String madeBy = "Mr. Spock";

    private final EventBusService eventBusService;
    private final BaristaRepository baristaRepository;

    public BaristaImpl(EventBusService eventBusService, BaristaRepository baristaRepository) {
        this.eventBusService = eventBusService;
        this.baristaRepository = baristaRepository;
    }

    @Transactional
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
        baristaRepository.save(baristaItem);
        eventBusService.sendToOrdersUp(JsonUtil.toJson(orderUp));
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
