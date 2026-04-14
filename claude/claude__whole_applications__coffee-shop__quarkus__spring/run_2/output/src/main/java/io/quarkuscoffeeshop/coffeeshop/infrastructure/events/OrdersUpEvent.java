package io.quarkuscoffeeshop.coffeeshop.infrastructure.events;

public class OrdersUpEvent {

    private final String payload;

    public OrdersUpEvent(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
