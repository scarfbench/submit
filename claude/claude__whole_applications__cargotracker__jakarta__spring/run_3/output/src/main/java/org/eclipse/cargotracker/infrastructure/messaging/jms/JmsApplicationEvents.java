package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

@Component
public class JmsApplicationEvents implements ApplicationEvents {

  private static final Logger logger = Logger.getLogger(JmsApplicationEvents.class.getName());

  @Autowired
  private ApplicationEventPublisher eventPublisher;

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
}
