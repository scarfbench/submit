package io.quarkuscoffeeshop.coffeeshop.kitchen;

import io.quarkuscoffeeshop.coffeeshop.domain.Item;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn;
import io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderUp;
import io.quarkuscoffeeshop.coffeeshop.kitchen.domain.KitchenOrder;
import io.quarkuscoffeeshop.coffeeshop.kitchen.domain.KitchenOrderRepository;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusService;
import io.quarkuscoffeeshop.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.Instant;

@Service
public class KitchenImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(KitchenImpl.class);

    String madeBy = "Sulu";

    private final EventBusService eventBusService;
    private final KitchenOrderRepository kitchenOrderRepository;

    public KitchenImpl(EventBusService eventBusService, KitchenOrderRepository kitchenOrderRepository) {
        this.eventBusService = eventBusService;
        this.kitchenOrderRepository = kitchenOrderRepository;
    }

    @PostConstruct
    public void init() {
        eventBusService.registerKitchenInHandler(this::onOrderIn);
    }

    @Transactional
    public void onOrderIn(final String orderInJson) {
        OrderIn orderIn = JsonUtil.fromJson(orderInJson, OrderIn.class);
        KitchenOrder kitchenOrder = new KitchenOrder(orderIn.orderId, orderIn.item, Instant.now());
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
        kitchenOrder.setTimeUp(Instant.now());
        kitchenOrderRepository.save(kitchenOrder);
        eventBusService.sendToOrdersUp(JsonUtil.toJson(orderUp));
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
