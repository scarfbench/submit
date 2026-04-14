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
import org.springframework.stereotype.Service;

/**
 * Spring ApplicationEvent-based implementation of ApplicationEvents.
 * Replaces JMS messaging with Spring's built-in event system for simplicity.
 */
@Service
public class JmsApplicationEvents implements ApplicationEvents, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(JmsApplicationEvents.class.getName());

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void cargoWasHandled(HandlingEvent event) {
        Cargo cargo = event.getCargo();
        logger.log(Level.INFO, "Cargo was handled {0}", cargo);
        eventPublisher.publishEvent(new CargoHandledEvent(this, cargo.getTrackingId().getIdString()));
    }

    @Override
    public void cargoWasMisdirected(Cargo cargo) {
        logger.log(Level.INFO, "Cargo was misdirected {0}", cargo);
        eventPublisher.publishEvent(new CargoMisdirectedEvent(this, cargo.getTrackingId().getIdString()));
    }

    @Override
    public void cargoHasArrived(Cargo cargo) {
        logger.log(Level.INFO, "Cargo has arrived {0}", cargo);
        eventPublisher.publishEvent(new CargoDeliveredEvent(this, cargo.getTrackingId().getIdString()));
    }

    @Override
    public void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt) {
        logger.log(Level.INFO, "Received handling event registration attempt {0}", attempt);
        eventPublisher.publishEvent(new HandlingEventRegistrationAttemptEvent(this, attempt));
    }

    // Inner event classes
    public static class CargoHandledEvent extends org.springframework.context.ApplicationEvent {
        private final String trackingId;
        public CargoHandledEvent(Object source, String trackingId) {
            super(source);
            this.trackingId = trackingId;
        }
        public String getTrackingId() { return trackingId; }
    }

    public static class CargoMisdirectedEvent extends org.springframework.context.ApplicationEvent {
        private final String trackingId;
        public CargoMisdirectedEvent(Object source, String trackingId) {
            super(source);
            this.trackingId = trackingId;
        }
        public String getTrackingId() { return trackingId; }
    }

    public static class CargoDeliveredEvent extends org.springframework.context.ApplicationEvent {
        private final String trackingId;
        public CargoDeliveredEvent(Object source, String trackingId) {
            super(source);
            this.trackingId = trackingId;
        }
        public String getTrackingId() { return trackingId; }
    }

    public static class HandlingEventRegistrationAttemptEvent extends org.springframework.context.ApplicationEvent {
        private final HandlingEventRegistrationAttempt attempt;
        public HandlingEventRegistrationAttemptEvent(Object source, HandlingEventRegistrationAttempt attempt) {
            super(source);
            this.attempt = attempt;
        }
        public HandlingEventRegistrationAttempt getAttempt() { return attempt; }
    }
}
