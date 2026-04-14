package org.eclipse.cargotracker.interfaces.booking.web;

import java.time.LocalDate;
import java.util.List;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRoute;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoStatus;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.Location;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.RouteCandidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for cargo administration operations.
 * Replaces JSF backing beans (ListCargo, CargoDetails, Track, Booking, ItinerarySelection,
 * ChangeDestination, ChangeArrivalDeadline) with REST API endpoints.
 */
@RestController
@RequestMapping("/api/cargo")
public class CargoAdminController {

    @Autowired
    private BookingServiceFacade bookingServiceFacade;

    @GetMapping("/list")
    public ResponseEntity<List<CargoRoute>> listAllCargo() {
        List<CargoRoute> cargos = bookingServiceFacade.listAllCargos();
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/{trackingId}")
    public ResponseEntity<CargoRoute> getCargo(@PathVariable String trackingId) {
        CargoRoute cargo = bookingServiceFacade.loadCargoForRouting(trackingId);
        return ResponseEntity.ok(cargo);
    }

    @GetMapping("/{trackingId}/track")
    public ResponseEntity<CargoStatus> trackCargo(@PathVariable String trackingId) {
        CargoStatus status = bookingServiceFacade.loadCargoForTracking(trackingId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @PostMapping("/book")
    public ResponseEntity<String> bookCargo(
            @RequestParam String originUnLocode,
            @RequestParam String destinationUnLocode,
            @RequestParam String arrivalDeadline) {
        LocalDate deadline = LocalDate.parse(arrivalDeadline);
        String trackingId = bookingServiceFacade.bookNewCargo(originUnLocode, destinationUnLocode, deadline);
        return ResponseEntity.ok(trackingId);
    }

    @GetMapping("/locations")
    public ResponseEntity<List<Location>> listLocations() {
        List<Location> locations = bookingServiceFacade.listShippingLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{trackingId}/routes")
    public ResponseEntity<List<RouteCandidate>> requestRoutes(@PathVariable String trackingId) {
        List<RouteCandidate> routes = bookingServiceFacade.requestPossibleRoutesForCargo(trackingId);
        return ResponseEntity.ok(routes);
    }

    @PostMapping("/{trackingId}/assignRoute")
    public ResponseEntity<Void> assignRoute(
            @PathVariable String trackingId,
            @RequestBody RouteCandidate route) {
        bookingServiceFacade.assignCargoToRoute(trackingId, route);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{trackingId}/changeDestination")
    public ResponseEntity<Void> changeDestination(
            @PathVariable String trackingId,
            @RequestParam String destinationUnLocode) {
        bookingServiceFacade.changeDestination(trackingId, destinationUnLocode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{trackingId}/changeDeadline")
    public ResponseEntity<Void> changeDeadline(
            @PathVariable String trackingId,
            @RequestParam String deadline) {
        LocalDate newDeadline = LocalDate.parse(deadline);
        bookingServiceFacade.changeDeadline(trackingId, newDeadline);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trackingIds")
    public ResponseEntity<List<String>> listTrackingIds() {
        List<String> trackingIds = bookingServiceFacade.listAllTrackingIds();
        return ResponseEntity.ok(trackingIds);
    }
}
