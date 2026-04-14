package io.quarkuscoffeeshop.coffeeshop.infrastructure.events;

public class KitchenInEvent {

    private final String payload;

    public KitchenInEvent(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
