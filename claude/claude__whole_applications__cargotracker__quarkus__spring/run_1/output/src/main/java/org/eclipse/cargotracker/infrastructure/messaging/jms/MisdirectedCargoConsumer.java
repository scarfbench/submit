package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MisdirectedCargoConsumer {

  private static final Logger logger = Logger.getLogger(MisdirectedCargoConsumer.class.getName());

  @JmsListener(destination = "${app.jms.MisdirectedCargoQueue}")
  public void onMessage(String trackingIdString) {
    logger.log(Level.INFO, "Cargo with tracking ID {0} misdirected.", trackingIdString);
  }
}
