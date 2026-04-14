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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.Instant;

@Service
public class KitchenImpl implements Kitchen {

    private static final Logger LOGGER = LoggerFactory.getLogger(KitchenImpl.class);

    String madeBy = "Sulu";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final KitchenOrderRepository kitchenOrderRepository;

    public KitchenImpl(ApplicationEventPublisher applicationEventPublisher, KitchenOrderRepository kitchenOrderRepository) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.kitchenOrderRepository = kitchenOrderRepository;
    }

    @EventListener(KitchenInEvent.class)
    @Transactional
    public void onKitchenInEvent(final KitchenInEvent event) {
        onOrderIn(event.getPayload());
    }

    @Override
    @Transactional
    public void onOrderIn(final String message) {
        OrderIn orderIn = JsonUtil.fromJson(message, OrderIn.class);
        KitchenOrder kitchenOrder = new KitchenOrder(orderIn.orderId, orderIn.item, Instant.now());
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
        kitchenOrder.setTimeUp(Instant.now());
        kitchenOrderRepository.save(kitchenOrder);
        applicationEventPublisher.publishEvent(new OrdersUpEvent(JsonUtil.toJson(orderUp)));
    }

    private int calculateDelay(final Item item) {
        switch (item) {
            case CROISSANT:
                return 7000;
            case CROISSANT_CHOCOLATE:
                return 7000;
            case CAKEPOP:
                return 5000;
            case MUFFIN:
                return 7000;
            default:
                return 3000;
        }
    }
}
