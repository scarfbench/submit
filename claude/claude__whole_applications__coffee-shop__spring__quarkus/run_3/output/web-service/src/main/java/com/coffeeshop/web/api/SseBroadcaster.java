package com.coffeeshop.web.api;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SseBroadcaster {

    /**
     * BroadcastProcessor for multicasting web updates to all connected SSE clients.
     * This replaces the Spring Reactor Sinks.Many<String> from the original implementation.
     */
    private final BroadcastProcessor<String> processor = BroadcastProcessor.create();

    /**
     * Emit a message to all subscribers.
     */
    public void broadcast(String message) {
        processor.onNext(message);
    }

    /**
     * Get a Multi stream for SSE clients to subscribe to.
     */
    public Multi<String> getStream() {
        return processor;
    }
}
