package io.quarkuscoffeeshop.coffeeshop.kitchen;

import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.KitchenInEvent;

public interface Kitchen {

    public void onOrderIn(KitchenInEvent event);
}
