package org.eclipse.cargotracker.infrastructure.messaging.cdi;

/**
 * CDI event fired when a handling event registration attempt is rejected.
 */
public class RejectedRegistrationAttemptEvent {

  private final String trackingId;

  public RejectedRegistrationAttemptEvent(String trackingId) {
    this.trackingId = trackingId;
  }

  public String getTrackingId() {
    return trackingId;
  }
}
