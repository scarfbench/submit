package org.eclipse.cargotracker.application.internal;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.handling.HandlingHistory;

@Service
@Transactional
public class DefaultCargoInspectionService implements CargoInspectionService {

  private static final Logger logger = Logger.getLogger(DefaultCargoInspectionService.class.getName());

  @Autowired private ApplicationEvents applicationEvents;
  @Autowired private CargoRepository cargoRepository;
  @Autowired private HandlingEventRepository handlingEventRepository;

  @Override
  public void inspectCargo(TrackingId trackingId) {
    Cargo cargo = cargoRepository.find(trackingId);
    if (cargo == null) {
      logger.log(Level.WARNING, "Can''t inspect non-existing cargo {0}", trackingId);
      return;
    }
    HandlingHistory handlingHistory =
        handlingEventRepository.lookupHandlingHistoryOfCargo(trackingId);
    cargo.deriveDeliveryProgress(handlingHistory);
    if (cargo.getDelivery().isMisdirected()) {
      applicationEvents.cargoWasMisdirected(cargo);
    }
    if (cargo.getDelivery().isUnloadedAtDestination()) {
      applicationEvents.cargoHasArrived(cargo);
    }
    cargoRepository.store(cargo);
  }
}
