package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveredCargoConsumer {

    @Autowired
    private Logger logger;

    @JmsListener(destination = "${app.jms.DeliveredCargoQueue}")
    public void onMessage(String trackingId) {
        logger.log(Level.INFO, "Cargo with tracking ID {0} delivered.", trackingId);
    }
}
