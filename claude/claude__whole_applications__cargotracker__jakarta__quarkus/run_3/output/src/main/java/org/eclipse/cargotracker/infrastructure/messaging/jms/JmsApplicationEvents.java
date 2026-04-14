package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

@ApplicationScoped
public class JmsApplicationEvents implements ApplicationEvents, Serializable {

  private static final long serialVersionUID = 1L;

  @Inject private Logger logger;

  @Inject private Event<CargoHandledEvent> cargoHandledEvent;
  @Inject private Event<MisdirectedCargoEvent> misdirectedCargoEvent;
  @Inject private Event<DeliveredCargoEvent> deliveredCargoEvent;
  @Inject private Event<HandlingEventRegistrationAttempt> handlingEventRegistrationEvent;

  @Override
  public void cargoWasHandled(HandlingEvent event) {
    Cargo cargo = event.getCargo();
    logger.log(Level.INFO, "Cargo was handled {0}", cargo);
    cargoHandledEvent.fire(new CargoHandledEvent(cargo.getTrackingId().getIdString()));
  }

  @Override
  public void cargoWasMisdirected(Cargo cargo) {
    logger.log(Level.INFO, "Cargo was misdirected {0}", cargo);
    misdirectedCargoEvent.fire(new MisdirectedCargoEvent(cargo.getTrackingId().getIdString()));
  }

  @Override
  public void cargoHasArrived(Cargo cargo) {
    logger.log(Level.INFO, "Cargo has arrived {0}", cargo);
    deliveredCargoEvent.fire(new DeliveredCargoEvent(cargo.getTrackingId().getIdString()));
  }

  @Override
  public void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt) {
    logger.log(Level.INFO, "Received handling event registration attempt {0}", attempt);
    handlingEventRegistrationEvent.fire(attempt);
  }

  // CDI Event classes
  public static class CargoHandledEvent {
    private final String trackingId;
    public CargoHandledEvent(String trackingId) { this.trackingId = trackingId; }
    public String getTrackingId() { return trackingId; }
  }

  public static class MisdirectedCargoEvent {
    private final String trackingId;
    public MisdirectedCargoEvent(String trackingId) { this.trackingId = trackingId; }
    public String getTrackingId() { return trackingId; }
  }

  public static class DeliveredCargoEvent {
    private final String trackingId;
    public DeliveredCargoEvent(String trackingId) { this.trackingId = trackingId; }
    public String getTrackingId() { return trackingId; }
  }
}
