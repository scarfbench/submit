package org.eclipse.cargotracker.infrastructure.messaging.cdi;

import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

/**
 * CDI event fired when a handling event registration attempt is received.
 */
public class HandlingEventRegistrationAttemptEvent {

  private final HandlingEventRegistrationAttempt attempt;

  public HandlingEventRegistrationAttemptEvent(HandlingEventRegistrationAttempt attempt) {
    this.attempt = attempt;
  }

  public HandlingEventRegistrationAttempt getAttempt() {
    return attempt;
  }
}
