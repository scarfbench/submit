package org.eclipse.cargotracker.infrastructure.messaging;

public class CargoArrivedEvent {
    private final String trackingId;

    public CargoArrivedEvent(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getTrackingId() {
        return trackingId;
    }
}
