package org.eclipse.cargotracker.application;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

public interface ApplicationEvents {
  void cargoWasHandled(HandlingEvent event);
  void cargoWasMisdirected(Cargo cargo);
  void cargoHasArrived(Cargo cargo);
  void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt);
}
