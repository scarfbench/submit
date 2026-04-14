package org.eclipse.cargotracker.interfaces.booking.rest;

import java.time.LocalDate;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.*;

@ApplicationScoped
@Path("/booking")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingRestService {

    @Inject
    BookingServiceFacade bookingServiceFacade;

    @GET
    @Path("/cargos")
    public List<CargoRoute> listAllCargos() {
        return bookingServiceFacade.listAllCargos();
    }

    @GET
    @Path("/locations")
    public List<Location> listLocations() {
        return bookingServiceFacade.listShippingLocations();
    }

    @GET
    @Path("/cargos/{trackingId}")
    public CargoRoute getCargo(@PathParam("trackingId") String trackingId) {
        return bookingServiceFacade.loadCargoForRouting(trackingId);
    }

    @GET
    @Path("/tracking/{trackingId}")
    public CargoStatus trackCargo(@PathParam("trackingId") String trackingId) {
        return bookingServiceFacade.loadCargoForTracking(trackingId);
    }

    @POST
    @Path("/cargos")
    @Transactional
    public Response bookCargo(BookCargoRequest request) {
        String trackingId = bookingServiceFacade.bookNewCargo(
            request.getOrigin(),
            request.getDestination(),
            request.getArrivalDeadline());
        return Response.ok(new BookCargoResponse(trackingId)).build();
    }

    @GET
    @Path("/cargos/{trackingId}/routes")
    public List<RouteCandidate> requestRoutes(@PathParam("trackingId") String trackingId) {
        return bookingServiceFacade.requestPossibleRoutesForCargo(trackingId);
    }

    @PUT
    @Path("/cargos/{trackingId}/route")
    @Transactional
    public Response assignRoute(@PathParam("trackingId") String trackingId, RouteCandidate route) {
        bookingServiceFacade.assignCargoToRoute(trackingId, route);
        return Response.ok().build();
    }

    @GET
    @Path("/trackingids")
    public List<String> listTrackingIds() {
        return bookingServiceFacade.listAllTrackingIds();
    }
}
