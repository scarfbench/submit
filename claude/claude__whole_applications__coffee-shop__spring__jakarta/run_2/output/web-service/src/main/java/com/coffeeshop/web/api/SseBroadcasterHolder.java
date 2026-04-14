package com.coffeeshop.web.api;

import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;

/**
 * Thread-safe singleton holder for the Jakarta SSE Broadcaster.
 * Replaces Spring's Reactor Sinks.Many with Jakarta JAX-RS SseBroadcaster.
 */
public final class SseBroadcasterHolder {

    private static volatile SseBroadcaster broadcaster;
    private static final Object lock = new Object();

    private SseBroadcasterHolder() {}

    /**
     * Get or lazily create the SSE broadcaster.
     */
    public static SseBroadcaster getBroadcaster(Sse sse) {
        if (broadcaster == null) {
            synchronized (lock) {
                if (broadcaster == null) {
                    broadcaster = sse.newBroadcaster();
                }
            }
        }
        return broadcaster;
    }
}
