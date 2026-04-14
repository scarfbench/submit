package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class DeliveredCargoConsumer {

  @Inject private Logger logger;

  public void onCargoDelivered(@Observes JmsApplicationEvents.DeliveredCargoEvent event) {
    logger.log(Level.INFO, "Cargo with tracking ID {0} delivered.", event.getTrackingId());
  }
}
