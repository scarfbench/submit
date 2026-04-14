package io.quarkuscoffeeshop.coffeeshop.counter.api;

import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;

/**
 * Coordinates order functions
 */
public interface OrderService {

    void onOrderIn(final PlaceOrderCommand placeOrderCommand);

    void onOrderUp(final String orderUpJson);
}
