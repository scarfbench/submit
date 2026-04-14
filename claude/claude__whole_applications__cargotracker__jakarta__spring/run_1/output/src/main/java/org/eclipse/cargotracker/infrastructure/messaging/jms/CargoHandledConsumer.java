package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Consumes Spring events and delegates notification of cargo handling to the cargo inspection
 * service.
 *
 * <p>This is a programmatic hook into the Spring event infrastructure to make cargo inspection
 * event-driven.
 */
@Component
public class CargoHandledConsumer {

  private static final Logger logger = Logger.getLogger(CargoHandledConsumer.class.getName());

  @Autowired private CargoInspectionService cargoInspectionService;

  @Async
  @EventListener
  public void onCargoHandled(JmsApplicationEvents.CargoHandledEvent event) {
    logger.log(Level.INFO, "Processing cargo handled event for {0}", event.getTrackingId());
    cargoInspectionService.inspectCargo(new TrackingId(event.getTrackingId()));
  }
}
