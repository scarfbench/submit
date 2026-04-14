package org.eclipse.cargotracker.infrastructure.messaging.events;

import java.io.Serializable;

/**
 * CDI event wrapper for cargo delivered events.
 */
public class CargoDeliveredEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String trackingId;

    public CargoDeliveredEvent(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    @Override
    public String toString() {
        return "CargoDeliveredEvent{trackingId='" + trackingId + "'}";
    }
}
