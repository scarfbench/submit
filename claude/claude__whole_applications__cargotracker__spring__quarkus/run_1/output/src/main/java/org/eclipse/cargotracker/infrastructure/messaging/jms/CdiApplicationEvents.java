package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class CdiApplicationEvents implements ApplicationEvents {

    @Inject
    Event<CargoHandledEvent> cargoHandledEvent;

    @Inject
    Event<CargoMisdirectedEvent> cargoMisdirectedEvent;

    @Inject
    Event<CargoDeliveredEvent> cargoDeliveredEvent;

    @Inject
    Event<HandlingEventRegistrationAttempt> handlingEventRegistrationAttemptEvent;

    @Inject
    Logger logger;

    @Override
    public void cargoWasHandled(HandlingEvent event) {
        Cargo cargo = event.getCargo();
        logger.log(Level.INFO, "Cargo was handled {0}", cargo);
        cargoHandledEvent.fireAsync(new CargoHandledEvent(cargo.getTrackingId().getIdString()));
    }

    @Override
    public void cargoWasMisdirected(Cargo cargo) {
        logger.log(Level.INFO, "Cargo was misdirected {0}", cargo);
        cargoMisdirectedEvent.fireAsync(new CargoMisdirectedEvent(cargo.getTrackingId().getIdString()));
    }

    @Override
    public void cargoHasArrived(Cargo cargo) {
        logger.log(Level.INFO, "Cargo has arrived {0}", cargo);
        cargoDeliveredEvent.fireAsync(new CargoDeliveredEvent(cargo.getTrackingId().getIdString()));
    }

    @Override
    public void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt) {
        logger.log(Level.INFO, "Received handling event registration attempt {0}", attempt);
        handlingEventRegistrationAttemptEvent.fireAsync(attempt);
    }

    // Inner event classes
    public static class CargoHandledEvent {
        private final String trackingId;
        public CargoHandledEvent(String trackingId) { this.trackingId = trackingId; }
        public String getTrackingId() { return trackingId; }
    }

    public static class CargoMisdirectedEvent {
        private final String trackingId;
        public CargoMisdirectedEvent(String trackingId) { this.trackingId = trackingId; }
        public String getTrackingId() { return trackingId; }
    }

    public static class CargoDeliveredEvent {
        private final String trackingId;
        public CargoDeliveredEvent(String trackingId) { this.trackingId = trackingId; }
        public String getTrackingId() { return trackingId; }
    }
}
