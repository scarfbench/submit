package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class HandlingEventRegistrationAttemptConsumer {

    @Autowired
    private Logger logger;

    @Autowired
    private HandlingEventService handlingEventService;

    @JmsListener(destination = "${app.jms.HandlingEventRegistrationAttemptQueue}")
    public void onMessage(HandlingEventRegistrationAttempt attempt) {
        try {
            logger.log(Level.INFO, "Handling event registration attempt received");
            handlingEventService.registerHandlingEvent(
                attempt.getCompletionTime(),
                attempt.getTrackingId(),
                attempt.getVoyageNumber(),
                attempt.getUnLocode(),
                attempt.getType());
        } catch (CannotCreateHandlingEventException e) {
            throw new RuntimeException("Error occurred processing message", e);
        }
    }
}
