package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DeliveredCargoConsumer {

    @Autowired
    private Logger logger;

    @EventListener
    @Async
    public void onCargoDelivered(JmsApplicationEvents.CargoArrivedEvent event) {
        logger.log(Level.INFO, "Cargo with tracking ID {0} delivered.", event.getTrackingId());
    }
}
