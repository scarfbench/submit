package org.eclipse.cargotracker.interfaces.booking.rest;

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
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing booking functionality as a JSON API.
 * Replaces JSF-based web interface for Spring Boot migration.
 */
@RestController
@RequestMapping("/rest/booking")
public class BookingRestController {

    @Autowired
    private BookingServiceFacade bookingServiceFacade;

    @GetMapping("/locations")
    public List<Location> listLocations() {
        return bookingServiceFacade.listShippingLocations();
    }

    @GetMapping("/cargos")
    public List<CargoRoute> listCargos() {
        return bookingServiceFacade.listAllCargos();
    }

    @GetMapping("/tracking-ids")
    public List<String> listTrackingIds() {
        return bookingServiceFacade.listAllTrackingIds();
    }

    @GetMapping("/cargo/{trackingId}")
    public ResponseEntity<CargoStatus> getCargoStatus(@PathVariable String trackingId) {
        CargoStatus status = bookingServiceFacade.loadCargoForTracking(trackingId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/cargo/{trackingId}/route")
    public CargoRoute getCargoRoute(@PathVariable String trackingId) {
        return bookingServiceFacade.loadCargoForRouting(trackingId);
    }

    @GetMapping("/cargo/{trackingId}/route-candidates")
    public List<RouteCandidate> getRouteCandidates(@PathVariable String trackingId) {
        return bookingServiceFacade.requestPossibleRoutesForCargo(trackingId);
    }

    @PostMapping("/cargo")
    public String bookNewCargo(@RequestBody BookCargoRequest request) {
        return bookingServiceFacade.bookNewCargo(
            request.getOrigin(), request.getDestination(), request.getArrivalDeadline());
    }

    @PostMapping("/cargo/{trackingId}/assign-route")
    public void assignRoute(@PathVariable String trackingId, @RequestBody RouteCandidate route) {
        bookingServiceFacade.assignCargoToRoute(trackingId, route);
    }

    @PostMapping("/cargo/{trackingId}/change-destination")
    public void changeDestination(@PathVariable String trackingId,
                                   @RequestBody ChangeDestinationRequest request) {
        bookingServiceFacade.changeDestination(trackingId, request.getDestination());
    }

    @PostMapping("/cargo/{trackingId}/change-deadline")
    public void changeDeadline(@PathVariable String trackingId,
                                @RequestBody ChangeDeadlineRequest request) {
        bookingServiceFacade.changeDeadline(trackingId, request.getArrivalDeadline());
    }

    // Request DTOs
    public static class BookCargoRequest {
        private String origin;
        private String destination;
        private LocalDate arrivalDeadline;

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public LocalDate getArrivalDeadline() { return arrivalDeadline; }
        public void setArrivalDeadline(LocalDate arrivalDeadline) { this.arrivalDeadline = arrivalDeadline; }
    }

    public static class ChangeDestinationRequest {
        private String destination;

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
    }

    public static class ChangeDeadlineRequest {
        private LocalDate arrivalDeadline;

        public LocalDate getArrivalDeadline() { return arrivalDeadline; }
        public void setArrivalDeadline(LocalDate arrivalDeadline) { this.arrivalDeadline = arrivalDeadline; }
    }
}
