package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import java.util.List;

public interface LocationRepository {
  Location find(UnLocode unLocode);
  List<Location> findAll();
  void store(Location location);
}
