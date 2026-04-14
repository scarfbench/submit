package com.coffeeshop.web.api;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@Path("/api/dashboard")
public class DashboardResource {

    private static final Logger log = LoggerFactory.getLogger(DashboardResource.class);

    @Inject
    SseBroadcaster sseBroadcaster;

    /**
     * SSE endpoint that streams updates to connected clients.
     * Starts with an init event, then broadcasts all subsequent updates.
     */
    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> stream() {
        log.debug("Client connected to dashboard stream");

        // Start with an init event so the client knows it's connected
        String initMessage = "dashboard stream connected at " + Instant.now();

        // Prepend the init message to the broadcast stream
        return Multi.createFrom().item(initMessage)
            .onItem().invoke(item -> log.debug("Sending init event: {}", item))
            .onCompletion().switchTo(sseBroadcaster.getStream());
    }
}
