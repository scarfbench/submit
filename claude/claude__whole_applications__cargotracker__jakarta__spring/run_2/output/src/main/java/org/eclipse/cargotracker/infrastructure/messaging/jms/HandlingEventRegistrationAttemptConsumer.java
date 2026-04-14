package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class HandlingEventRegistrationAttemptConsumer {

    @Autowired
    private HandlingEventService handlingEventService;

    @Autowired
    private Logger logger;

    @EventListener
    @Async
    public void onHandlingEventRegistration(JmsApplicationEvents.HandlingEventRegistrationEvent event) {
        try {
            HandlingEventRegistrationAttempt attempt = event.getAttempt();
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
