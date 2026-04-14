package org.eclipse.cargotracker.interfaces.tracking.web;

import java.util.List;
import java.util.logging.Logger;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for public cargo tracking. This interface sits immediately on top of the domain
 * layer, unlike the booking interface which has a facade and supporting DTOs in between.
 */
@RestController
@RequestMapping("/api/track")
public class Track {

  private static final Logger logger = Logger.getLogger(Track.class.getName());

  @Autowired private CargoRepository cargoRepository;
  @Autowired private HandlingEventRepository handlingEventRepository;

  @GetMapping
  public ResponseEntity<CargoTrackingViewAdapter> trackById(
      @RequestParam("trackingId") String trackingId) {
    Cargo cargo = cargoRepository.find(new TrackingId(trackingId));

    if (cargo != null) {
      List<HandlingEvent> handlingEvents =
          handlingEventRepository
              .lookupHandlingHistoryOfCargo(new TrackingId(trackingId))
              .getDistinctEventsByCompletionTime();
      CargoTrackingViewAdapter adapter = new CargoTrackingViewAdapter(cargo, handlingEvents);
      return ResponseEntity.ok(adapter);
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
