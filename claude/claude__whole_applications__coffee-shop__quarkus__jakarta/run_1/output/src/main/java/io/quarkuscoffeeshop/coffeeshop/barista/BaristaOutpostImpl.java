package io.quarkuscoffeeshop.coffeeshop.barista;

import io.quarkuscoffeeshop.coffeeshop.barista.domain.BaristaItem;
import io.quarkuscoffeeshop.coffeeshop.barista.domain.BaristaRepository;
import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.BaristaInEvent;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.OrdersUpEvent;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;

/**
 * Barista implementation that processes beverage orders.
 * Replaces the original Quarkus BaristaOutpostImpl + BaristaImpl.
 * Uses CDI events instead of Vert.x EventBus and Kafka messaging.
 */
@ApplicationScoped
public class BaristaOutpostImpl {

    static final Logger LOGGER = LoggerFactory.getLogger(BaristaOutpostImpl.class);

    private final String madeBy = "Mr. Spock";

    @Inject
    Event<OrdersUpEvent> ordersUpEvent;

    @Inject
    BaristaRepository baristaRepository;

    @Transactional
    public void observeBaristaIn(@Observes BaristaInEvent event) {
        OrderIn orderIn = JsonUtil.fromJsonToOrderIn(event.getJson());
        BaristaItem baristaItem = new BaristaItem();
        baristaItem.setItem(orderIn.item.toString());
        baristaItem.setTimeIn(Instant.now());
        LOGGER.debug("barista order in : {}", orderIn);

        // Simulate preparation delay (reduced for responsiveness)
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
        ordersUpEvent.fire(new OrdersUpEvent(JsonUtil.toJson(orderUp)));
    }

    private int calculateDelay(final Item item) {
        switch (item) {
            case COFFEE_BLACK:
                return 500;
            case COFFEE_WITH_ROOM:
                return 500;
            case ESPRESSO:
                return 700;
            case ESPRESSO_DOUBLE:
                return 700;
            case CAPPUCCINO:
                return 1000;
            default:
                return 300;
        }
    }
}
