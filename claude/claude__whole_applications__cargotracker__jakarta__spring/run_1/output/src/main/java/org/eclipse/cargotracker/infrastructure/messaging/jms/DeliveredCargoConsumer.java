package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DeliveredCargoConsumer {

  private static final Logger logger = Logger.getLogger(DeliveredCargoConsumer.class.getName());

  @Async
  @EventListener
  public void onCargoDelivered(JmsApplicationEvents.CargoDeliveredEvent event) {
    logger.log(Level.INFO, "Cargo with tracking ID {0} delivered.", event.getTrackingId());
  }
}
