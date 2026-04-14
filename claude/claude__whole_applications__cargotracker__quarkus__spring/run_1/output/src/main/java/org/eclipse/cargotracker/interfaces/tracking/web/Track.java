package org.eclipse.cargotracker.interfaces.tracking.web;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for public cargo tracking. This interface sits immediately on top of the domain
 * layer, unlike the booking interface which has a facade and supporting DTOs in between.
 */
@RestController
@RequestMapping("/rest/public/track")
public class Track {

    private static final Logger logger = Logger.getLogger(Track.class.getName());

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private HandlingEventRepository handlingEventRepository;

    @GetMapping("/{trackingId}")
    public ResponseEntity<CargoTrackingViewAdapter> trackById(@PathVariable String trackingId) {
        String trimmedId = trackingId.trim();
        Cargo cargo = cargoRepository.find(new TrackingId(trimmedId));

        if (cargo != null) {
            List<HandlingEvent> handlingEvents =
                    handlingEventRepository
                            .lookupHandlingHistoryOfCargo(new TrackingId(trimmedId))
                            .getDistinctEventsByCompletionTime();
            CargoTrackingViewAdapter adapter = new CargoTrackingViewAdapter(cargo, handlingEvents);
            return ResponseEntity.ok(adapter);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
