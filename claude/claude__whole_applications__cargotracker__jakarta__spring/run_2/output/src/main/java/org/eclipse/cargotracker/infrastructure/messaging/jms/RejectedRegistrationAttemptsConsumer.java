package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RejectedRegistrationAttemptsConsumer {

    @Autowired
    private Logger logger;

    public void onRejectedRegistration(String trackingId) {
        logger.log(Level.INFO, "Rejected registration attempt of cargo with tracking ID {0}.", trackingId);
    }
}
