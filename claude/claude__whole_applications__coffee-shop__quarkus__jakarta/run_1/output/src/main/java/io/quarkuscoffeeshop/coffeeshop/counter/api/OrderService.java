package io.quarkuscoffeeshop.coffeeshop.counter.api;

import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.OrdersUpEvent;

/**
 * Coordinates order functions
 */
public interface OrderService {

    public void onOrderIn(final PlaceOrderCommand placeOrderCommand);

    public void onOrderUp(final OrdersUpEvent event);
}
