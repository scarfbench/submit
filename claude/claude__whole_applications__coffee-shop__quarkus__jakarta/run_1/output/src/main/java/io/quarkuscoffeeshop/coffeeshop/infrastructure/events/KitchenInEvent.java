package io.quarkuscoffeeshop.coffeeshop.infrastructure.events;

/**
 * CDI event wrapper for kitchen order-in messages.
 */
public class KitchenInEvent {
    private final String json;

    public KitchenInEvent(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }
}
