package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.WEB_UPDATES;

@ApplicationScoped
public class DashboardBroadcaster {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardBroadcaster.class);

    private final List<SseClient> clients = new CopyOnWriteArrayList<>();

    @Inject
    CdiEventBus eventBus;

    @PostConstruct
    void init() {
        eventBus.registerConsumer(WEB_UPDATES, this::broadcast);
    }

    public void register(SseEventSink eventSink, Sse sse) {
        clients.add(new SseClient(eventSink, sse));
        LOGGER.debug("SSE client registered, total clients: {}", clients.size());
    }

    private void broadcast(String message) {
        clients.removeIf(client -> client.sink.isClosed());
        for (SseClient client : clients) {
            try {
                if (!client.sink.isClosed()) {
                    client.sink.send(client.sse.newEvent(message));
                }
            } catch (Exception e) {
                LOGGER.debug("Error sending SSE event: {}", e.getMessage());
            }
        }
    }

    private record SseClient(SseEventSink sink, Sse sse) {}
}
