package io.quarkuscoffeeshop.coffeeshop.infrastructure.events;

/**
 * CDI event wrapper for barista order-in messages.
 */
public class BaristaInEvent {
    private final String json;

    public BaristaInEvent(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }
}
