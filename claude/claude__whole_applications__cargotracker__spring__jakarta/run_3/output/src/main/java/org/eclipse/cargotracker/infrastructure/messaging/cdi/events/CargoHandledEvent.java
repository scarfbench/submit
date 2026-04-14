package org.eclipse.cargotracker.infrastructure.messaging.cdi.events;

import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

/**
 * CDI event fired when a cargo has been handled.
 */
public class CargoHandledEvent {

  private final TrackingId trackingId;

  public CargoHandledEvent(TrackingId trackingId) {
    this.trackingId = trackingId;
  }

  public TrackingId getTrackingId() {
    return trackingId;
  }
}
