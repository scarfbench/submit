package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

@ApplicationScoped
public class CargoHandledConsumer {

  @Inject Logger logger;
  @Inject CargoInspectionService cargoInspectionService;

  public void onCargoHandled(@ObservesAsync JmsApplicationEvents.CargoHandledEvent event) {
    cargoInspectionService.inspectCargo(new TrackingId(event.getTrackingId()));
  }
}
