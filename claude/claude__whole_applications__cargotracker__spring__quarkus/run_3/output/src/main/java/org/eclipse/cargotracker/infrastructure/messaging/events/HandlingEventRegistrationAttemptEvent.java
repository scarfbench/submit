package org.eclipse.cargotracker.infrastructure.messaging.events;

import java.io.Serializable;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

/**
 * CDI event wrapper for handling event registration attempt events.
 */
public class HandlingEventRegistrationAttemptEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HandlingEventRegistrationAttempt attempt;

    public HandlingEventRegistrationAttemptEvent(HandlingEventRegistrationAttempt attempt) {
        this.attempt = attempt;
    }

    public HandlingEventRegistrationAttempt getAttempt() {
        return attempt;
    }

    @Override
    public String toString() {
        return "HandlingEventRegistrationAttemptEvent{attempt=" + attempt + "}";
    }
}
