package io.quarkuscoffeeshop.coffeeshop.barista.api;

import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.BaristaInEvent;

/**
 * Barista API for processing beverage orders
 */
public interface Barista {

    /**
     * Handle an incoming barista order event
     *
     * @param event BaristaInEvent containing JSON payload of OrderIn
     * @see io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn
     */
    public void onOrderIn(final BaristaInEvent event);
}