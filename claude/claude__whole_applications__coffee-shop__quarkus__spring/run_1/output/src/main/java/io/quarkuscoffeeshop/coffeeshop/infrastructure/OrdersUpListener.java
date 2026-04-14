package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Connects the EventBusService orders-up channel to the OrderService
 */
@Component
public class OrdersUpListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersUpListener.class);

    private final EventBusService eventBusService;
    private final OrderService orderService;

    public OrdersUpListener(EventBusService eventBusService, OrderService orderService) {
        this.eventBusService = eventBusService;
        this.orderService = orderService;
    }

    @PostConstruct
    public void init() {
        eventBusService.registerOrdersUpHandler(json -> {
            LOGGER.debug("OrderUp received, forwarding to OrderService: {}", json);
            orderService.onOrderUp(json);
        });
    }
}
