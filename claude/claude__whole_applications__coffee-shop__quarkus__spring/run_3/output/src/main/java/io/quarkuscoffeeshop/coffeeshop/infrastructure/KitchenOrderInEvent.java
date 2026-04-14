package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import org.springframework.context.ApplicationEvent;

public class KitchenOrderInEvent extends ApplicationEvent {
    private final String orderInJson;

    public KitchenOrderInEvent(Object source, String orderInJson) {
        super(source);
        this.orderInJson = orderInJson;
    }

    public String getOrderInJson() {
        return orderInJson;
    }
}
