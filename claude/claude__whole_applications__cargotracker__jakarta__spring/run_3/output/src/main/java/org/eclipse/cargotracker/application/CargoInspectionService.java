package org.eclipse.cargotracker.application;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

public interface CargoInspectionService {
  void inspectCargo(
      @NotNull(message = "Tracking ID is required.") @Valid TrackingId trackingId);
}
