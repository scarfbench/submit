package org.eclipse.cargotracker.domain.model.cargo;

import java.util.List;

public interface CargoRepository {

  Cargo find(TrackingId trackingId);

  void store(Cargo cargo);

  TrackingId nextTrackingId();

  List<Cargo> findAll();
}
