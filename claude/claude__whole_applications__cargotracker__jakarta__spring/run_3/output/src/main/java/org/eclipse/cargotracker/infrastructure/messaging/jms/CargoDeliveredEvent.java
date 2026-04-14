package org.eclipse.cargotracker.infrastructure.messaging.jms;

public class CargoDeliveredEvent {
  private final String trackingId;

  public CargoDeliveredEvent(String trackingId) {
    this.trackingId = trackingId;
  }

  public String getTrackingId() {
    return trackingId;
  }
}
