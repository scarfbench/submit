package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MisdirectedCargoConsumer {

  private static final Logger logger = Logger.getLogger(MisdirectedCargoConsumer.class.getName());

  @EventListener
  public void onCargoMisdirected(CargoMisdirectedEvent event) {
    logger.log(Level.INFO, "Cargo with tracking ID {0} misdirected.", event.getTrackingId());
  }
}
