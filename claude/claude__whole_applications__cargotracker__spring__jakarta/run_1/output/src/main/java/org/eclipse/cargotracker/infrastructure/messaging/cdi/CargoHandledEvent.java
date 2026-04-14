package org.eclipse.cargotracker.infrastructure.messaging.cdi;

import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

/**
 * CDI event fired when cargo is handled.
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
