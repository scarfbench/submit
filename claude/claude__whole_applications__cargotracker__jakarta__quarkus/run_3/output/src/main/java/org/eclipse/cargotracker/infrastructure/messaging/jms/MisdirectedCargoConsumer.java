package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class MisdirectedCargoConsumer {

  @Inject private Logger logger;

  public void onCargoMisdirected(@Observes JmsApplicationEvents.MisdirectedCargoEvent event) {
    logger.log(Level.INFO, "Cargo with tracking ID {0} misdirected.", event.getTrackingId());
  }
}
