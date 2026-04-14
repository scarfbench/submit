package org.eclipse.cargotracker.infrastructure.messaging.jms;

import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

public class HandlingEventRegistrationAttemptEvent {
  private final HandlingEventRegistrationAttempt attempt;

  public HandlingEventRegistrationAttemptEvent(HandlingEventRegistrationAttempt attempt) {
    this.attempt = attempt;
  }

  public HandlingEventRegistrationAttempt getAttempt() {
    return attempt;
  }
}
