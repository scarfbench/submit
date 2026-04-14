package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RejectedRegistrationAttemptsConsumer {

  @Inject Logger logger;

  public void logRejection(String trackingId) {
    logger.log(Level.INFO, "Rejected registration attempt of cargo with tracking ID {0}.", trackingId);
  }
}
