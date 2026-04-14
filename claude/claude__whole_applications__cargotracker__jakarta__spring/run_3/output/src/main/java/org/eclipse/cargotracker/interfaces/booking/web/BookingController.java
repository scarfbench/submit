package org.eclipse.cargotracker.interfaces.booking.web;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRoute;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoStatus;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.Location;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.RouteCandidate;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

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
  public ResponseEntity<CargoRoute> getCargo(@PathVariable String trackingId) {
    CargoRoute cargo = bookingServiceFacade.loadCargoForRouting(trackingId);
    if (cargo == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(cargo);
  }

  @GetMapping("/cargo/{trackingId}/status")
  public ResponseEntity<CargoStatus> trackCargo(@PathVariable String trackingId) {
    CargoStatus status = bookingServiceFacade.loadCargoForTracking(trackingId);
    if (status == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(status);
  }

  @PostMapping("/cargos")
  public Map<String, String> bookCargo(@RequestBody BookingRequest request) {
    String trackingId = bookingServiceFacade.bookNewCargo(
        request.getOrigin(), request.getDestination(), request.getArrivalDeadline());
    Map<String, String> response = new HashMap<>();
    response.put("trackingId", trackingId);
    return response;
  }

  @GetMapping("/cargo/{trackingId}/routes")
  public List<RouteCandidate> requestRoutes(@PathVariable String trackingId) {
    return bookingServiceFacade.requestPossibleRoutesForCargo(trackingId);
  }

  @PostMapping("/cargo/{trackingId}/route")
  public void assignRoute(@PathVariable String trackingId, @RequestBody RouteCandidate route) {
    bookingServiceFacade.assignCargoToRoute(trackingId, route);
  }

  @PutMapping("/cargo/{trackingId}/destination")
  public void changeDestination(@PathVariable String trackingId,
      @RequestBody Map<String, String> body) {
    bookingServiceFacade.changeDestination(trackingId, body.get("destination"));
  }

  @PutMapping("/cargo/{trackingId}/deadline")
  public void changeDeadline(@PathVariable String trackingId,
      @RequestBody Map<String, String> body) {
    bookingServiceFacade.changeDeadline(trackingId,
        LocalDate.parse(body.get("arrivalDeadline")));
  }

  public static class BookingRequest {
    private String origin;
    private String destination;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate arrivalDeadline;

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDate getArrivalDeadline() { return arrivalDeadline; }
    public void setArrivalDeadline(LocalDate arrivalDeadline) { this.arrivalDeadline = arrivalDeadline; }
  }
}
