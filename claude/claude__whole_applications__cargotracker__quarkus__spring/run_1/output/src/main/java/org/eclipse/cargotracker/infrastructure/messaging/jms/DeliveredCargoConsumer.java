package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveredCargoConsumer {

  private static final Logger logger = Logger.getLogger(DeliveredCargoConsumer.class.getName());

  @JmsListener(destination = "${app.jms.DeliveredCargoQueue}")
  public void onMessage(String trackingIdString) {
    logger.log(Level.INFO, "Cargo with tracking ID {0} delivered.", trackingIdString);
  }
}
