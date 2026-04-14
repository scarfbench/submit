package org.eclipse.cargotracker.infrastructure.messaging.jms;

/**
 * CDI event fired when a cargo has been handled.
 */
public class CargoHandledEvent {

  private final String trackingId;

  public CargoHandledEvent(String trackingId) {
    this.trackingId = trackingId;
  }

  public String getTrackingId() {
    return trackingId;
  }
}
