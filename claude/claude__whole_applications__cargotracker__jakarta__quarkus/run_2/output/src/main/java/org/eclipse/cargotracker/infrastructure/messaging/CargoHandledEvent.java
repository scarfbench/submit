package org.eclipse.cargotracker.infrastructure.messaging;

public class CargoHandledEvent {
    private final String trackingId;

    public CargoHandledEvent(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getTrackingId() {
        return trackingId;
    }
}
