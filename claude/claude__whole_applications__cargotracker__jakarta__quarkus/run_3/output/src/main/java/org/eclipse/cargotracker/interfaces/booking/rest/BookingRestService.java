package org.eclipse.cargotracker.interfaces.booking.rest;

import java.time.LocalDate;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRoute;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoStatus;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.Location;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.RouteCandidate;

/**
 * REST API for the Cargo Tracker booking operations.
 * Replaces the JSF-based booking interface.
 */
@ApplicationScoped
@Path("/booking")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingRestService {

    @Inject
    private BookingServiceFacade bookingServiceFacade;

    @GET
    @Path("/locations")
    public List<Location> listLocations() {
        return bookingServiceFacade.listShippingLocations();
    }

    @GET
    @Path("/cargos")
    public List<CargoRoute> listCargos() {
        return bookingServiceFacade.listAllCargos();
    }

    @GET
    @Path("/cargos/{trackingId}")
    public Response getCargo(@PathParam("trackingId") String trackingId) {
        CargoRoute cargo = bookingServiceFacade.loadCargoForRouting(trackingId);
        if (cargo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(cargo).build();
    }

    @POST
    @Path("/cargos")
    public Response bookCargo(BookCargoRequest request) {
        String trackingId = bookingServiceFacade.bookNewCargo(
            request.getOrigin(), request.getDestination(), request.getArrivalDeadline());
        return Response.ok(new BookCargoResponse(trackingId)).build();
    }

    @GET
    @Path("/cargos/{trackingId}/routes")
    public List<RouteCandidate> requestRoutes(@PathParam("trackingId") String trackingId) {
        return bookingServiceFacade.requestPossibleRoutesForCargo(trackingId);
    }

    @PUT
    @Path("/cargos/{trackingId}/route")
    public Response assignRoute(@PathParam("trackingId") String trackingId, RouteCandidate route) {
        bookingServiceFacade.assignCargoToRoute(trackingId, route);
        return Response.ok().build();
    }

    @PUT
    @Path("/cargos/{trackingId}/destination")
    public Response changeDestination(@PathParam("trackingId") String trackingId,
                                      ChangeDestinationRequest request) {
        bookingServiceFacade.changeDestination(trackingId, request.getDestination());
        return Response.ok().build();
    }

    @PUT
    @Path("/cargos/{trackingId}/deadline")
    public Response changeDeadline(@PathParam("trackingId") String trackingId,
                                   ChangeDeadlineRequest request) {
        bookingServiceFacade.changeDeadline(trackingId, request.getArrivalDeadline());
        return Response.ok().build();
    }

    // Request/Response DTOs
    public static class BookCargoRequest {
        private String origin;
        private String destination;
        private LocalDate arrivalDeadline;

        public BookCargoRequest() {}

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public LocalDate getArrivalDeadline() { return arrivalDeadline; }
        public void setArrivalDeadline(LocalDate arrivalDeadline) { this.arrivalDeadline = arrivalDeadline; }
    }

    public static class BookCargoResponse {
        private String trackingId;

        public BookCargoResponse() {}
        public BookCargoResponse(String trackingId) { this.trackingId = trackingId; }
        public String getTrackingId() { return trackingId; }
        public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
    }

    public static class ChangeDestinationRequest {
        private String destination;

        public ChangeDestinationRequest() {}
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
    }

    public static class ChangeDeadlineRequest {
        private LocalDate arrivalDeadline;

        public ChangeDeadlineRequest() {}
        public LocalDate getArrivalDeadline() { return arrivalDeadline; }
        public void setArrivalDeadline(LocalDate arrivalDeadline) { this.arrivalDeadline = arrivalDeadline; }
    }
}
