package org.eclipse.cargotracker.infrastructure.messaging.jms;

import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Consumes handling event registration attempt events and delegates to proper registration. */
@Component
public class HandlingEventRegistrationAttemptConsumer {

  @Autowired private HandlingEventService handlingEventService;

  @Async
  @EventListener
  public void onHandlingEventRegistrationAttempt(
      JmsApplicationEvents.HandlingEventRegistrationAttemptEvent event) {
    try {
      HandlingEventRegistrationAttempt attempt = event.getAttempt();
      handlingEventService.registerHandlingEvent(
          attempt.getCompletionTime(),
          attempt.getTrackingId(),
          attempt.getVoyageNumber(),
          attempt.getUnLocode(),
          attempt.getType());
    } catch (CannotCreateHandlingEventException e) {
      // In Spring, exceptions in async event listeners should be handled appropriately
      throw new RuntimeException("Error occurred processing event", e);
    }
  }
}
