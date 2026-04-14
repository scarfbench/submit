package org.eclipse.cargotracker.infrastructure.messaging.cdi.events;

import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

/**
 * CDI event fired when a cargo has been delivered.
 */
public class DeliveredCargoEvent {

  private final TrackingId trackingId;

  public DeliveredCargoEvent(TrackingId trackingId) {
    this.trackingId = trackingId;
  }

  public TrackingId getTrackingId() {
    return trackingId;
  }
}
