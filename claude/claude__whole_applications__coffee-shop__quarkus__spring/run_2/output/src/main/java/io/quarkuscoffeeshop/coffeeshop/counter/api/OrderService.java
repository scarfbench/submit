package io.quarkuscoffeeshop.coffeeshop.counter.api;

import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;

/**
 * Coordinates order functions
 */
public interface OrderService {

    void onOrderIn(PlaceOrderCommand placeOrderCommand);

    void onOrderUp(String message);
}
