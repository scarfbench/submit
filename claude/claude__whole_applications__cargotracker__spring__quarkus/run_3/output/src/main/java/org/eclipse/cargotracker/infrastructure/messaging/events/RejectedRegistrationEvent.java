package org.eclipse.cargotracker.infrastructure.messaging.events;

import java.io.Serializable;

/**
 * CDI event wrapper for rejected registration attempt events.
 */
public class RejectedRegistrationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String trackingId;

    public RejectedRegistrationEvent(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    @Override
    public String toString() {
        return "RejectedRegistrationEvent{trackingId='" + trackingId + "'}";
    }
}
