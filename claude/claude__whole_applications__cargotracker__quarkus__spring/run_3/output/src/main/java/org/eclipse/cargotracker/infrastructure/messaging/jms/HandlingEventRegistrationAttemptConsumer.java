package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class HandlingEventRegistrationAttemptConsumer {

    private static final Logger logger = Logger.getLogger(HandlingEventRegistrationAttemptConsumer.class.getName());

    @Autowired
    private HandlingEventService handlingEventService;

    @EventListener
    public void onMessage(JmsApplicationEvents.HandlingEventRegistrationAttemptEvent event) {
        HandlingEventRegistrationAttempt attempt = event.getAttempt();
        logger.log(Level.INFO, "HandlingEventRegistrationAttemptConsumer received: {0}", attempt);
        try {
            handlingEventService.registerHandlingEvent(
                    attempt.getCompletionTime(),
                    attempt.getTrackingId(),
                    attempt.getVoyageNumber(),
                    attempt.getUnLocode(),
                    attempt.getType());
        } catch (CannotCreateHandlingEventException e) {
            logger.log(Level.SEVERE, "Error processing handling event registration", e);
        }
    }
}
