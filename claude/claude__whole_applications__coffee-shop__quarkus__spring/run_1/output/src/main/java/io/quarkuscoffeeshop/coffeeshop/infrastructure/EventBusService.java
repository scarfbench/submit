package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Replaces the Vert.x EventBus with Spring's ApplicationEventPublisher
 * and direct method invocations for internal messaging.
 */
@Service
public class EventBusService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBusService.class);

    private final ApplicationEventPublisher eventPublisher;

    // These will be set by the beans that handle the events
    private java.util.function.Consumer<String> baristaInHandler;
    private java.util.function.Consumer<String> kitchenInHandler;
    private java.util.function.Consumer<String> ordersUpHandler;
    private java.util.function.Consumer<String> webUpdateHandler;

    public EventBusService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void registerBaristaInHandler(java.util.function.Consumer<String> handler) {
        this.baristaInHandler = handler;
    }

    public void registerKitchenInHandler(java.util.function.Consumer<String> handler) {
        this.kitchenInHandler = handler;
    }

    public void registerOrdersUpHandler(java.util.function.Consumer<String> handler) {
        this.ordersUpHandler = handler;
    }

    public void registerWebUpdateHandler(java.util.function.Consumer<String> handler) {
        this.webUpdateHandler = handler;
    }

    public void publishWebUpdate(String json) {
        LOGGER.debug("Publishing web update: {}", json);
        if (webUpdateHandler != null) {
            webUpdateHandler.accept(json);
        }
    }

    public void sendToBaristaIn(String json) {
        LOGGER.debug("Sending to barista-in: {}", json);
        if (baristaInHandler != null) {
            baristaInHandler.accept(json);
        }
    }

    public void sendToKitchenIn(String json) {
        LOGGER.debug("Sending to kitchen-in: {}", json);
        if (kitchenInHandler != null) {
            kitchenInHandler.accept(json);
        }
    }

    public void sendToOrdersUp(String json) {
        LOGGER.debug("Sending to orders-up: {}", json);
        if (ordersUpHandler != null) {
            ordersUpHandler.accept(json);
        }
    }
}
