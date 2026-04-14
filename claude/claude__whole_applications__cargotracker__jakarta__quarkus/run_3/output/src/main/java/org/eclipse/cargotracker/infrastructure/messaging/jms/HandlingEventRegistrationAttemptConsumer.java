package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

@ApplicationScoped
public class HandlingEventRegistrationAttemptConsumer {

  @Inject private HandlingEventService handlingEventService;
  @Inject private Logger logger;

  public void onHandlingEventRegistration(@Observes HandlingEventRegistrationAttempt attempt) {
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
