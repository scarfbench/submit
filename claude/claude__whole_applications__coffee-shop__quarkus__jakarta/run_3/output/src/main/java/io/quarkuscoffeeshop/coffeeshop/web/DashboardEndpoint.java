package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkuscoffeeshop.coffeeshop.infrastructure.CdiEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import static io.quarkuscoffeeshop.coffeeshop.infrastructure.EventBusTopics.WEB_UPDATES;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Path("/dashboard")
@ApplicationScoped
public class DashboardEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardEndpoint.class);

    @Inject
    CdiEventBus eventBus;

    private final List<SseEventSink> sinks = new CopyOnWriteArrayList<>();
    private Sse sse;

    @PostConstruct
    void init() {
        eventBus.register(WEB_UPDATES, this::broadcast);
    }

    private void broadcast(String message) {
        if (sse == null) return;
        sinks.removeIf(SseEventSink::isClosed);
        sinks.forEach(sink -> {
            try {
                sink.send(sse.newEvent(message));
            } catch (Exception e) {
                LOGGER.debug("Failed to send SSE event: {}", e.getMessage());
            }
        });
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void dashboardStream(@Context SseEventSink eventSink, @Context Sse sse) {
        this.sse = sse;
        sinks.add(eventSink);
        LOGGER.debug("New SSE client connected. Total clients: {}", sinks.size());
    }
}
