package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CargoHandledConsumer {

    @Autowired
    private Logger logger;

    @Autowired
    private CargoInspectionService cargoInspectionService;

    @EventListener
    @Async
    public void onCargoHandled(JmsApplicationEvents.CargoHandledEvent event) {
        logger.log(Level.INFO, "Cargo handled event received for tracking ID: {0}", event.getTrackingId());
        cargoInspectionService.inspectCargo(new TrackingId(event.getTrackingId()));
    }
}
