package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.WebUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api")
public class CoffeeshopApiResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoffeeshopApiResource.class);

    private final OrderService orderService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public CoffeeshopApiResource(OrderService orderService, ApplicationEventPublisher applicationEventPublisher) {
        this.orderService = orderService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping(value = "/order",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Void> placeOrder(@RequestBody final PlaceOrderCommand placeOrderCommand) {

        LOGGER.info("PlaceOrderCommand received: {}", placeOrderCommand);
        orderService.onOrderIn(placeOrderCommand);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/message")
    public void sendMessage(@RequestBody final String message) {
        LOGGER.debug("received message: {}", message);
        LOGGER.debug("sending to web-updates: {}", message);
        applicationEventPublisher.publishEvent(new WebUpdateEvent(message));
    }

}
