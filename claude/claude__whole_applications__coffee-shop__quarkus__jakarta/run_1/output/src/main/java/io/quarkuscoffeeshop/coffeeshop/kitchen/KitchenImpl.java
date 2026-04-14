package io.quarkuscoffeeshop.coffeeshop.kitchen;

import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.KitchenInEvent;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.OrdersUpEvent;
import io.quarkuscoffeeshop.coffeeshop.kitchen.domain.KitchenOrder;
import io.quarkuscoffeeshop.coffeeshop.kitchen.domain.KitchenOrderRepository;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
public class KitchenImpl implements Kitchen {

    private static final Logger LOGGER = LoggerFactory.getLogger(KitchenImpl.class);

    String madeBy = "Sulu";

    @Inject
    Event<OrdersUpEvent> ordersUpEvent;

    @Inject
    KitchenOrderRepository kitchenOrderRepository;

    @Override
    @Transactional
    public void onOrderIn(final KitchenInEvent event) {
        OrderIn orderIn = JsonUtil.fromJson(event.getJson(), OrderIn.class);
        KitchenOrder kitchenOrder = new KitchenOrder(orderIn.orderId, orderIn.item, Instant.now());
        LOGGER.debug("kitchen order in : {}", orderIn);
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
        kitchenOrder.setTimeUp(Instant.now());
        kitchenOrderRepository.persist(kitchenOrder);
        ordersUpEvent.fire(new OrdersUpEvent(JsonUtil.toJson(orderUp)));
    }

    // CDI Observer
    @Transactional
    public void observeKitchenIn(@Observes KitchenInEvent event) {
        onOrderIn(event);
    }

    private int calculateDelay(final Item item) {
        switch (item) {
            case CROISSANT:
                return 700;
            case CROISSANT_CHOCOLATE:
                return 700;
            case CAKEPOP:
                return 500;
            case MUFFIN:
                return 700;
            default:
                return 300;
        }
    }
}
