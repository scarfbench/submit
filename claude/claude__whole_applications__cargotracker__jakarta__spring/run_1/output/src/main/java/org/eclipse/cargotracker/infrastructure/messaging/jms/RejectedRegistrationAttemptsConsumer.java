package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RejectedRegistrationAttemptsConsumer {

  private static final Logger logger =
      Logger.getLogger(RejectedRegistrationAttemptsConsumer.class.getName());

  @Async
  @EventListener
  public void onRejectedRegistrationAttempt(
      JmsApplicationEvents.RejectedRegistrationAttemptEvent event) {
    logger.log(
        Level.INFO,
        "Rejected registration attempt of cargo with tracking ID {0}.",
        event.getTrackingId());
  }

  // Note: This event class needs to be added to JmsApplicationEvents if not already present
  // or this consumer can be left as a placeholder for future implementation
}
