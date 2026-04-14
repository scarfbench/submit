package org.eclipse.cargotracker.infrastructure.messaging;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

@ApplicationScoped
public class CargoEventObservers {

    @Inject
    Logger logger;

    @Inject
    CargoInspectionService cargoInspectionService;

    @Inject
    HandlingEventService handlingEventService;

    public void onCargoHandled(@Observes CargoHandledEvent event) {
        logger.log(Level.INFO, "Processing cargo handled event for tracking ID: {0}", event.getTrackingId());
        cargoInspectionService.inspectCargo(new TrackingId(event.getTrackingId()));
    }

    public void onCargoMisdirected(@Observes CargoMisdirectedEvent event) {
        logger.log(Level.INFO, "Cargo with tracking ID {0} misdirected.", event.getTrackingId());
    }

    public void onCargoArrived(@Observes CargoArrivedEvent event) {
        logger.log(Level.INFO, "Cargo with tracking ID {0} delivered.", event.getTrackingId());
    }

    public void onHandlingEventRegistrationAttempt(@Observes HandlingEventRegistrationAttempt attempt) {
        logger.log(Level.INFO, "Processing handling event registration attempt {0}", attempt);
        try {
            handlingEventService.registerHandlingEvent(
                attempt.getCompletionTime(),
                attempt.getTrackingId(),
                attempt.getVoyageNumber(),
                attempt.getUnLocode(),
                attempt.getType());
        } catch (CannotCreateHandlingEventException e) {
            logger.log(Level.WARNING, "Rejected handling event registration attempt {0}", attempt);
        }
    }
}
