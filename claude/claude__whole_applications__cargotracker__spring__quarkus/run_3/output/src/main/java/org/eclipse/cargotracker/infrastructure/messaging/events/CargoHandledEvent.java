package org.eclipse.cargotracker.infrastructure.messaging.events;

import java.io.Serializable;

/**
 * CDI event wrapper for cargo handled events.
 */
public class CargoHandledEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String trackingId;

    public CargoHandledEvent(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    @Override
    public String toString() {
        return "CargoHandledEvent{trackingId='" + trackingId + "'}";
    }
}
