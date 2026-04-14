package org.eclipse.cargotracker.infrastructure.messaging.cdi;

/**
 * CDI event fired when cargo is misdirected.
 */
public class CargoMisdirectedEvent {

  private final String trackingId;

  public CargoMisdirectedEvent(String trackingId) {
    this.trackingId = trackingId;
  }

  public String getTrackingId() {
    return trackingId;
  }
}
