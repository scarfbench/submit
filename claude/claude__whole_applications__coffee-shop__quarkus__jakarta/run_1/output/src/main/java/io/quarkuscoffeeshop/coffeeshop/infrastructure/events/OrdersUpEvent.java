package io.quarkuscoffeeshop.coffeeshop.infrastructure.events;

/**
 * CDI event wrapper for orders-up messages.
 */
public class OrdersUpEvent {
    private final String json;

    public OrdersUpEvent(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }
}
