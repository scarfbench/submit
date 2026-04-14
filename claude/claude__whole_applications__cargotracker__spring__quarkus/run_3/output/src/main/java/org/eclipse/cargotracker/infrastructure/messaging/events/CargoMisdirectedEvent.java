package org.eclipse.cargotracker.infrastructure.messaging.events;

import java.io.Serializable;

/**
 * CDI event wrapper for cargo misdirected events.
 */
public class CargoMisdirectedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String trackingId;

    public CargoMisdirectedEvent(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    @Override
    public String toString() {
        return "CargoMisdirectedEvent{trackingId='" + trackingId + "'}";
    }
}
