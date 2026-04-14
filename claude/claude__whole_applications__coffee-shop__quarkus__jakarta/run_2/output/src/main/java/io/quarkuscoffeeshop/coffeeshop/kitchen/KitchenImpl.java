package io.quarkuscoffeeshop.coffeeshop.kitchen;

import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.CdiEventBus;
import io.quarkuscoffeeshop.coffeeshop.kitchen.domain.KitchenOrder;
import io.quarkuscoffeeshop.coffeeshop.kitchen.domain.KitchenOrderRepository;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;

import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.*;

@ApplicationScoped
public class KitchenImpl implements Kitchen {

    private static final Logger LOGGER = LoggerFactory.getLogger(KitchenImpl.class);

    String madeBy = "Sulu";

    @Inject
    CdiEventBus eventBus;

    @Inject
    KitchenOrderRepository kitchenOrderRepository;

    @PostConstruct
    void init() {
        eventBus.registerConsumer(KITCHEN_IN, this::handleKitchenIn);
    }

    @Transactional
    private void handleKitchenIn(String messageBody) {
        onOrderIn(messageBody);
    }

    @Override
    @Transactional
    public void onOrderIn(final String messageBody) {
        OrderIn orderIn = JsonUtil.fromJson(messageBody, OrderIn.class);
        KitchenOrder kitchenOrder = new KitchenOrder(orderIn.orderId, orderIn.item, Instant.now());
        LOGGER.debug("order in : {}", orderIn);
        try {
            Thread.sleep(calculateDelay(orderIn.item));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        OrderUp orderUp = new OrderUp(
                orderIn.orderId, orderIn.itemId, orderIn.item,
                orderIn.name, Instant.now(), madeBy);
        kitchenOrder.setTimeUp(Instant.now());
        kitchenOrderRepository.persist(kitchenOrder);
        eventBus.publish(ORDERS_UP, JsonUtil.toJson(orderUp));
    }

    private int calculateDelay(final Item item) {
        switch (item) {
            case CROISSANT: return 7000;
            case CROISSANT_CHOCOLATE: return 7000;
            case CAKEPOP: return 5000;
            case MUFFIN: return 7000;
            default: return 3000;
        }
    }
}
