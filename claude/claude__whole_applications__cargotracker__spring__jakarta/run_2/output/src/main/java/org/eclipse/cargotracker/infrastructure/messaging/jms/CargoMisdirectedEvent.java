package org.eclipse.cargotracker.infrastructure.messaging.jms;

/**
 * CDI event fired when a cargo has been misdirected.
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
