package org.eclipse.cargotracker.application;

import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.location.UnLocode;

public interface BookingService {
  TrackingId bookNewCargo(
      @NotNull(message = "Origin is required.") @Valid UnLocode origin,
      @NotNull(message = "Destination is required.") @Valid UnLocode destination,
      @NotNull(message = "Deadline is required.")
          @Future(message = "Deadline must be in the future.")
          LocalDate arrivalDeadline);

  List<Itinerary> requestPossibleRoutesForCargo(
      @NotNull(message = "Tracking ID is required.") @Valid TrackingId trackingId);

  void assignCargoToRoute(
      @NotNull(message = "Itinerary is required.") @Valid Itinerary itinerary,
      @NotNull(message = "Tracking ID is required.") @Valid TrackingId trackingId);

  void changeDestination(
      @NotNull(message = "Tracking ID is required.") @Valid TrackingId trackingId,
      @NotNull(message = "Destination is required.") @Valid UnLocode unLocode);

  void changeDeadline(
      @NotNull(message = "Tracking ID is required.") @Valid TrackingId trackingId,
      @NotNull(message = "Deadline is required.")
          @Future(message = "Deadline must be in the future.")
          LocalDate deadline);
}
