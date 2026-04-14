package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import org.springframework.context.ApplicationEvent;

public class WebUpdateEvent extends ApplicationEvent {
    private final String updateJson;

    public WebUpdateEvent(Object source, String updateJson) {
        super(source);
        this.updateJson = updateJson;
    }

    public String getUpdateJson() {
        return updateJson;
    }
}
