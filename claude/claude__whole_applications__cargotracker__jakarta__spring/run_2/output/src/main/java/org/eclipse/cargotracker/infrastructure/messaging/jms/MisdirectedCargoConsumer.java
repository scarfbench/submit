package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MisdirectedCargoConsumer {

    @Autowired
    private Logger logger;

    @EventListener
    @Async
    public void onCargoMisdirected(JmsApplicationEvents.CargoMisdirectedEvent event) {
        logger.log(Level.INFO, "Cargo with tracking ID {0} misdirected.", event.getTrackingId());
    }
}
