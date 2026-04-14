package io.quarkuscoffeeshop.coffeeshop.kitchen;

import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.KitchenOrderInEvent;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.OrderUpEvent;
import io.quarkuscoffeeshop.coffeeshop.kitchen.domain.KitchenOrder;
import io.quarkuscoffeeshop.coffeeshop.kitchen.domain.KitchenOrderRepository;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class KitchenImpl implements Kitchen {

    private static final Logger LOGGER = LoggerFactory.getLogger(KitchenImpl.class);

    String madeBy = "Sulu";

    private final ApplicationEventPublisher eventPublisher;
    private final KitchenOrderRepository kitchenOrderRepository;

    public KitchenImpl(ApplicationEventPublisher eventPublisher, KitchenOrderRepository kitchenOrderRepository) {
        this.eventPublisher = eventPublisher;
        this.kitchenOrderRepository = kitchenOrderRepository;
    }

    @EventListener
    @Async
    @Transactional
    public void handleKitchenOrderIn(KitchenOrderInEvent event) {
        onOrderIn(event.getOrderInJson());
    }

    @Override
    @Transactional
    public void onOrderIn(String orderInJson) {
        OrderIn orderIn = JsonUtil.fromJson(orderInJson, OrderIn.class);
        KitchenOrder kitchenOrder = new KitchenOrder(orderIn.orderId, orderIn.item, Instant.now());
        LOGGER.debug("order in : {}", orderIn);
        try {
            Thread.sleep(calculateDelay(orderIn.item));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while processing order", e);
        }
        OrderUp orderUp = new OrderUp(
                orderIn.orderId,
                orderIn.itemId,
                orderIn.item,
                orderIn.name,
                Instant.now(),
                madeBy);
        kitchenOrder.setTimeUp(Instant.now());
        kitchenOrderRepository.save(kitchenOrder);
        eventPublisher.publishEvent(new OrderUpEvent(this, JsonUtil.toJson(orderUp)));
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
