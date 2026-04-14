package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import org.springframework.context.ApplicationEvent;

public class BaristaOrderInEvent extends ApplicationEvent {
    private final String orderInJson;

    public BaristaOrderInEvent(Object source, String orderInJson) {
        super(source);
        this.orderInJson = orderInJson;
    }

    public String getOrderInJson() {
        return orderInJson;
    }
}
