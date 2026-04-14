package org.eclipse.cargotracker.infrastructure.messaging.cdi;

/**
 * CDI event fired when cargo is delivered.
 */
public class CargoDeliveredEvent {

  private final String trackingId;

  public CargoDeliveredEvent(String trackingId) {
    this.trackingId = trackingId;
  }

  public String getTrackingId() {
    return trackingId;
  }
}
