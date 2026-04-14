package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class JmsApplicationEvents implements ApplicationEvents, Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private Logger logger;

    @Override
    public void cargoWasHandled(HandlingEvent event) {
        Cargo cargo = event.getCargo();
        logger.log(Level.INFO, "Cargo was handled {0}", cargo);
        eventPublisher.publishEvent(new CargoHandledEvent(cargo.getTrackingId().getIdString()));
    }

    @Override
    public void cargoWasMisdirected(Cargo cargo) {
        logger.log(Level.INFO, "Cargo was misdirected {0}", cargo);
        eventPublisher.publishEvent(new CargoMisdirectedEvent(cargo.getTrackingId().getIdString()));
    }

    @Override
    public void cargoHasArrived(Cargo cargo) {
        logger.log(Level.INFO, "Cargo has arrived {0}", cargo);
        eventPublisher.publishEvent(new CargoArrivedEvent(cargo.getTrackingId().getIdString()));
    }

    @Override
    public void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt) {
        logger.log(Level.INFO, "Received handling event registration attempt {0}", attempt);
        eventPublisher.publishEvent(new HandlingEventRegistrationEvent(attempt));
    }

    // Spring Event classes (inner classes for simplicity)
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

    public static class CargoArrivedEvent {
        private final String trackingId;
        public CargoArrivedEvent(String trackingId) { this.trackingId = trackingId; }
        public String getTrackingId() { return trackingId; }
    }

    public static class HandlingEventRegistrationEvent {
        private final HandlingEventRegistrationAttempt attempt;
        public HandlingEventRegistrationEvent(HandlingEventRegistrationAttempt attempt) {
            this.attempt = attempt;
        }
        public HandlingEventRegistrationAttempt getAttempt() { return attempt; }
    }
}
