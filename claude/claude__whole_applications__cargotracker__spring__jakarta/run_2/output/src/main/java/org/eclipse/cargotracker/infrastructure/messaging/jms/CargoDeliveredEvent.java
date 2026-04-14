package org.eclipse.cargotracker.infrastructure.messaging.jms;

/**
 * CDI event fired when a cargo has arrived at its destination.
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
