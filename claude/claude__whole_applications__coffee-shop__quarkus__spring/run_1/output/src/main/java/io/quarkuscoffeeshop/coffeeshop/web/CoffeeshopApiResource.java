package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CoffeeshopApiResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoffeeshopApiResource.class);

    private final OrderService orderService;
    private final EventBusService eventBusService;

    public CoffeeshopApiResource(OrderService orderService, EventBusService eventBusService) {
        this.orderService = orderService;
        this.eventBusService = eventBusService;
    }

    @PostMapping("/order")
    public ResponseEntity<Void> placeOrder(@RequestBody final PlaceOrderCommand placeOrderCommand) {
        LOGGER.info("PlaceOrderCommand received: {}", placeOrderCommand);
        orderService.onOrderIn(placeOrderCommand);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/message")
    public void sendMessage(@RequestBody final String message) {
        LOGGER.debug("received message: {}", message);
        LOGGER.debug("sending to web-updates: {}", message);
        eventBusService.publishWebUpdate(message);
    }
}
