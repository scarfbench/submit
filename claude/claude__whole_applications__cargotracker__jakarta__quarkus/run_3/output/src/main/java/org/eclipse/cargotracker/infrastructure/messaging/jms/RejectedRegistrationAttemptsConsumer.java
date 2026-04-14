package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class RejectedRegistrationAttemptsConsumer {

  @Inject private Logger logger;

  // This consumer was for rejected attempts - in CDI events model, we'll observe a specific event
  // For now, we log a warning if we receive one
  public void onRejected(@Observes JmsApplicationEvents.CargoHandledEvent event) {
    // Placeholder - rejected registration attempts are now handled synchronously
    logger.log(Level.FINE, "Processing event (potential rejection check) for cargo {0}", event.getTrackingId());
  }
}
