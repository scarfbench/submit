package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.util.List;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;

public interface VoyageRepository {
  Voyage find(VoyageNumber voyageNumber);
  List<Voyage> findAll();
  void store(Voyage voyage);
}
