package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.WebUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;
import java.util.concurrent.CopyOnWriteArrayList;

@Path("/dashboard")
@ApplicationScoped
public class DashboardEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardEndpoint.class);

    private final CopyOnWriteArrayList<SseEventSink> sinks = new CopyOnWriteArrayList<>();
    private volatile Sse sse;

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void dashboardStream(@Context SseEventSink eventSink, @Context Sse sse) {
        this.sse = sse;
        sinks.add(eventSink);
        LOGGER.debug("New SSE client connected, total: {}", sinks.size());
    }

    public void onWebUpdate(@Observes WebUpdateEvent event) {
        if (sse == null) return;
        sinks.removeIf(SseEventSink::isClosed);
        for (SseEventSink sink : sinks) {
            try {
                if (!sink.isClosed()) {
                    sink.send(sse.newEvent(event.getJson()));
                }
            } catch (Exception e) {
                LOGGER.debug("Error sending SSE event: {}", e.getMessage());
            }
        }
    }
}
