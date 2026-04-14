package org.eclipse.cargotracker.domain.model.handling;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;

@Component
public class HandlingEventFactory {

  @Autowired private CargoRepository cargoRepository;
  @Autowired private LocationRepository locationRepository;
  @Autowired private VoyageRepository voyageRepository;

  public HandlingEvent createHandlingEvent(
      LocalDateTime registrationTime,
      LocalDateTime completionTime,
      TrackingId trackingId,
      VoyageNumber voyageNumber,
      UnLocode unlocodeId,
      HandlingEvent.Type type)
      throws CannotCreateHandlingEventException {
    org.eclipse.cargotracker.domain.model.cargo.Cargo cargo = findCargo(trackingId);
    Voyage voyage = findVoyage(voyageNumber);
    Location location = findLocation(unlocodeId);

    try {
      if (voyage == null) {
        return new HandlingEvent(cargo, completionTime, registrationTime, type, location);
      } else {
        return new HandlingEvent(cargo, completionTime, registrationTime, type, location, voyage);
      }
    } catch (Exception e) {
      throw new CannotCreateHandlingEventException(e);
    }
  }

  private org.eclipse.cargotracker.domain.model.cargo.Cargo findCargo(TrackingId trackingId) throws UnknownCargoException {
    org.eclipse.cargotracker.domain.model.cargo.Cargo cargo = cargoRepository.find(trackingId);
    if (cargo == null) {
      throw new UnknownCargoException(trackingId);
    }
    return cargo;
  }

  private Voyage findVoyage(VoyageNumber voyageNumber) throws UnknownVoyageException {
    if (voyageNumber == null) {
      return null;
    }
    try {
      return voyageRepository.find(voyageNumber);
    } catch (Exception e) {
      throw new UnknownVoyageException(voyageNumber);
    }
  }

  private Location findLocation(UnLocode unlocode) throws UnknownLocationException {
    try {
      return locationRepository.find(unlocode);
    } catch (Exception e) {
      throw new UnknownLocationException(unlocode);
    }
  }
}
