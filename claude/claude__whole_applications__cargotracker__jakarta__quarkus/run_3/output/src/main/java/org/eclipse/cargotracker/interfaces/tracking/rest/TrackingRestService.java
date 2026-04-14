package org.eclipse.cargotracker.interfaces.tracking.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoStatus;

/**
 * REST API for the Cargo Tracker tracking operations.
 * Replaces the JSF-based tracking interface.
 */
@ApplicationScoped
@Path("/track")
@Produces(MediaType.APPLICATION_JSON)
public class TrackingRestService {

    @Inject
    private BookingServiceFacade bookingServiceFacade;

    @GET
    @Path("/{trackingId}")
    public Response trackCargo(@PathParam("trackingId") String trackingId) {
        CargoStatus cargoStatus = bookingServiceFacade.loadCargoForTracking(trackingId);
        if (cargoStatus == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Cargo not found for tracking ID: " + trackingId + "\"}")
                .build();
        }
        return Response.ok(cargoStatus).build();
    }
}
