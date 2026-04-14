package io.quarkuscoffeeshop.coffeeshop.infrastructure.events;

public class WebUpdateEvent {

    private final String payload;

    public WebUpdateEvent(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
