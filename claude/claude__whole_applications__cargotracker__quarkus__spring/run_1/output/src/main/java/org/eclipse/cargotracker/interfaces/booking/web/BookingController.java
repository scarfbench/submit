package org.eclipse.cargotracker.interfaces.booking.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRoute;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoStatus;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.Location;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.RouteCandidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that replaces the JSF-based booking UI beans.
 * Provides all booking, tracking, routing, and cargo management endpoints.
 *
 * <p>This controller consolidates functionality from the following JSF beans:
 * <ul>
 *   <li>Booking.java - Cargo booking operations</li>
 *   <li>Track.java - Cargo tracking operations</li>
 *   <li>ListCargo.java - Cargo listing operations</li>
 *   <li>CargoDetails.java - Cargo detail retrieval</li>
 *   <li>ItinerarySelection.java - Route assignment operations</li>
 *   <li>ChangeDestination.java - Destination change operations</li>
 *   <li>ChangeArrivalDeadline.java - Deadline change operations</li>
 * </ul>
 *
 * <p>Operates against a dedicated service facade (BookingServiceFacade), and is completely
 * separated from the domain layer. This approach keeps the domain model shielded from
 * user interface considerations.
 */
@RestController
@RequestMapping("/rest/booking")
public class BookingController {

    @Autowired
    private BookingServiceFacade bookingServiceFacade;

    // ============================================================
    // Location Management
    // ============================================================

    /**
     * Lists all available shipping locations.
     * Replaces: Booking.getLocations() and ChangeDestination.getLocations()
     *
     * @return list of all shipping locations
     */
    @GetMapping("/locations")
    public List<Location> listLocations() {
        return bookingServiceFacade.listShippingLocations();
    }

    // ============================================================
    // Cargo Booking
    // ============================================================

    /**
     * Books a new cargo with the specified origin, destination, and arrival deadline.
     * Replaces: Booking.register()
     *
     * @param originUnlocode the UN location code of the origin
     * @param destinationUnlocode the UN location code of the destination
     * @param arrivalDeadline the desired arrival deadline
     * @return response containing the tracking ID or error message
     */
    @PostMapping("/cargo")
    public ResponseEntity<Map<String, String>> bookNewCargo(
            @RequestParam String originUnlocode,
            @RequestParam String destinationUnlocode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalDeadline) {
        if (originUnlocode.equals(destinationUnlocode)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Origin and destination cannot be the same."));
        }
        String trackingId = bookingServiceFacade.bookNewCargo(originUnlocode, destinationUnlocode, arrivalDeadline);
        return ResponseEntity.ok(Map.of("trackingId", trackingId));
    }

    // ============================================================
    // Cargo Listing
    // ============================================================

    /**
     * Lists all cargos grouped by their routing status.
     * Replaces: ListCargo.init() and related getter methods
     *
     * @return map containing three lists: notRouted, routedUnclaimed, and claimed cargos
     */
    @GetMapping("/cargos")
    public Map<String, List<CargoRoute>> listAllCargos() {
        List<CargoRoute> cargos = bookingServiceFacade.listAllCargos();
        List<CargoRoute> notRouted = cargos.stream()
                .filter(c -> !c.isRouted())
                .collect(Collectors.toList());
        List<CargoRoute> routedUnclaimed = cargos.stream()
                .filter(c -> c.isRouted() && !c.isClaimed())
                .collect(Collectors.toList());
        List<CargoRoute> claimed = cargos.stream()
                .filter(CargoRoute::isClaimed)
                .collect(Collectors.toList());
        return Map.of(
                "notRouted", notRouted,
                "routedUnclaimed", routedUnclaimed,
                "claimed", claimed
        );
    }

    // ============================================================
    // Cargo Details
    // ============================================================

    /**
     * Retrieves detailed routing information for a specific cargo.
     * Replaces: CargoDetails.load() and ItinerarySelection.load()
     *
     * @param trackingId the tracking ID of the cargo
     * @return cargo routing details
     */
    @GetMapping("/cargo/{trackingId}")
    public CargoRoute getCargoDetails(@PathVariable String trackingId) {
        return bookingServiceFacade.loadCargoForRouting(trackingId);
    }

    // ============================================================
    // Cargo Tracking
    // ============================================================

    /**
     * Lists all available tracking IDs.
     * Replaces: Track.init()
     *
     * @return list of all tracking IDs
     */
    @GetMapping("/tracking/ids")
    public List<String> listTrackingIds() {
        return bookingServiceFacade.listAllTrackingIds();
    }

    /**
     * Retrieves tracking status information for a specific cargo.
     * Replaces: Track.onTrackById()
     *
     * @param trackingId the tracking ID of the cargo
     * @return cargo status information
     */
    @GetMapping("/tracking/{trackingId}")
    public CargoStatus trackCargo(@PathVariable String trackingId) {
        return bookingServiceFacade.loadCargoForTracking(trackingId);
    }

    // ============================================================
    // Route Management
    // ============================================================

    /**
     * Requests possible routes for a cargo.
     * Replaces: ItinerarySelection.load() (route candidates part)
     *
     * @param trackingId the tracking ID of the cargo
     * @return list of possible route candidates
     */
    @GetMapping("/cargo/{trackingId}/routes")
    public List<RouteCandidate> requestRoutes(@PathVariable String trackingId) {
        return bookingServiceFacade.requestPossibleRoutesForCargo(trackingId);
    }

    /**
     * Assigns a selected route to a cargo.
     * Replaces: ItinerarySelection.assignItinerary()
     *
     * @param trackingId the tracking ID of the cargo
     * @param route the selected route candidate
     * @return empty response on success
     */
    @PostMapping("/cargo/{trackingId}/assign-route")
    public ResponseEntity<Void> assignRoute(
            @PathVariable String trackingId,
            @RequestBody RouteCandidate route) {
        bookingServiceFacade.assignCargoToRoute(trackingId, route);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // Cargo Modifications
    // ============================================================

    /**
     * Changes the destination of a cargo.
     * Replaces: ChangeDestination.changeDestination()
     *
     * @param trackingId the tracking ID of the cargo
     * @param destinationUnlocode the new destination UN location code
     * @return empty response on success
     */
    @PutMapping("/cargo/{trackingId}/destination")
    public ResponseEntity<Void> changeDestination(
            @PathVariable String trackingId,
            @RequestParam String destinationUnlocode) {
        bookingServiceFacade.changeDestination(trackingId, destinationUnlocode);
        return ResponseEntity.ok().build();
    }

    /**
     * Changes the arrival deadline of a cargo.
     * Replaces: ChangeArrivalDeadline.changeArrivalDeadline()
     *
     * @param trackingId the tracking ID of the cargo
     * @param deadline the new arrival deadline
     * @return empty response on success
     */
    @PutMapping("/cargo/{trackingId}/deadline")
    public ResponseEntity<Void> changeDeadline(
            @PathVariable String trackingId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline) {
        bookingServiceFacade.changeDeadline(trackingId, deadline);
        return ResponseEntity.ok().build();
    }
}
