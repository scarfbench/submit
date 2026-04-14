package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;

@Component
public class RejectedRegistrationAttemptsConsumer {

  private static final Logger logger = Logger.getLogger(RejectedRegistrationAttemptsConsumer.class.getName());

  public void onRejectedRegistration(String trackingId) {
    logger.log(Level.INFO, "Rejected registration attempt of cargo with tracking ID {0}.", trackingId);
  }
}
