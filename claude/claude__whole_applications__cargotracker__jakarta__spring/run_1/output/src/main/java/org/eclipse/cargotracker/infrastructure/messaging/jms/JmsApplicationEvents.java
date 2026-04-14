package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class JmsApplicationEvents implements ApplicationEvents {

  private static final Logger logger = Logger.getLogger(JmsApplicationEvents.class.getName());

  @Autowired private ApplicationEventPublisher eventPublisher;

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
    eventPublisher.publishEvent(new CargoDeliveredEvent(cargo.getTrackingId().getIdString()));
  }

  @Override
  public void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt) {
    logger.log(Level.INFO, "Received handling event registration attempt {0}", attempt);
    eventPublisher.publishEvent(new HandlingEventRegistrationAttemptEvent(attempt));
  }

  // Spring Event classes
  public static class CargoHandledEvent {
    private final String trackingId;

    public CargoHandledEvent(String trackingId) {
      this.trackingId = trackingId;
    }

    public String getTrackingId() {
      return trackingId;
    }
  }

  public static class CargoMisdirectedEvent {
    private final String trackingId;

    public CargoMisdirectedEvent(String trackingId) {
      this.trackingId = trackingId;
    }

    public String getTrackingId() {
      return trackingId;
    }
  }

  public static class CargoDeliveredEvent {
    private final String trackingId;

    public CargoDeliveredEvent(String trackingId) {
      this.trackingId = trackingId;
    }

    public String getTrackingId() {
      return trackingId;
    }
  }

  public static class HandlingEventRegistrationAttemptEvent {
    private final HandlingEventRegistrationAttempt attempt;

    public HandlingEventRegistrationAttemptEvent(HandlingEventRegistrationAttempt attempt) {
      this.attempt = attempt;
    }

    public HandlingEventRegistrationAttempt getAttempt() {
      return attempt;
    }
  }

  public static class RejectedRegistrationAttemptEvent {
    private final String trackingId;

    public RejectedRegistrationAttemptEvent(String trackingId) {
      this.trackingId = trackingId;
    }

    public String getTrackingId() {
      return trackingId;
    }
  }
}
