package io.quarkuscoffeeshop.coffeeshop.infrastructure.events;

public class BaristaInEvent {

    private final String payload;

    public BaristaInEvent(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
