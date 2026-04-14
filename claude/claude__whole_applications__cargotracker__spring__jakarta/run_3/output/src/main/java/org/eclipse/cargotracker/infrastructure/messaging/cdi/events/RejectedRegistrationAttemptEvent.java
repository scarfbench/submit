package org.eclipse.cargotracker.infrastructure.messaging.cdi.events;

import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

/**
 * CDI event fired when a handling event registration attempt is rejected.
 */
public class RejectedRegistrationAttemptEvent {

  private final TrackingId trackingId;

  public RejectedRegistrationAttemptEvent(TrackingId trackingId) {
    this.trackingId = trackingId;
  }

  public TrackingId getTrackingId() {
    return trackingId;
  }
}
