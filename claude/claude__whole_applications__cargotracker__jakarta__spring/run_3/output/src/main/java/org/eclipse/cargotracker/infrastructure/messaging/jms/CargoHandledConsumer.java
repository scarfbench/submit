package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

@Component
public class CargoHandledConsumer {

  private static final Logger logger = Logger.getLogger(CargoHandledConsumer.class.getName());

  @Autowired
  private CargoInspectionService cargoInspectionService;

  @EventListener
  public void onCargoHandled(CargoHandledEvent event) {
    logger.log(Level.INFO, "Cargo handled event received for {0}", event.getTrackingId());
    cargoInspectionService.inspectCargo(new TrackingId(event.getTrackingId()));
  }
}
