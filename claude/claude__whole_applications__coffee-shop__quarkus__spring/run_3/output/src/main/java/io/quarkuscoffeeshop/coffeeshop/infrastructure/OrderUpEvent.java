package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import org.springframework.context.ApplicationEvent;

public class OrderUpEvent extends ApplicationEvent {
    private final String orderUpJson;

    public OrderUpEvent(Object source, String orderUpJson) {
        super(source);
        this.orderUpJson = orderUpJson;
    }

    public String getOrderUpJson() {
        return orderUpJson;
    }
}
