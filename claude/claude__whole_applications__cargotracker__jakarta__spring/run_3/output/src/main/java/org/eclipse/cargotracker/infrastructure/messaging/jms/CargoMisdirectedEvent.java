package org.eclipse.cargotracker.infrastructure.messaging.jms;

public class CargoMisdirectedEvent {
  private final String trackingId;

  public CargoMisdirectedEvent(String trackingId) {
    this.trackingId = trackingId;
  }

  public String getTrackingId() {
    return trackingId;
  }
}
