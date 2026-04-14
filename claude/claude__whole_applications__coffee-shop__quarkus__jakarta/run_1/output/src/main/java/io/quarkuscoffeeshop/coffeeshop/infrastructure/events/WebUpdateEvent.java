package io.quarkuscoffeeshop.coffeeshop.infrastructure.events;

/**
 * CDI event wrapper for web update messages (SSE streaming).
 */
public class WebUpdateEvent {
    private final String json;

    public WebUpdateEvent(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }
}
