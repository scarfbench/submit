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
 * REST resource for tracking operations. Replaces JSF-based tracking web UI.
 */
@ApplicationScoped
@Path("/track")
@Produces(MediaType.APPLICATION_JSON)
public class TrackingResource {

    @Inject
    BookingServiceFacade bookingServiceFacade;

    @GET
    @Path("/{trackingId}")
    public Response trackCargo(@PathParam("trackingId") String trackingId) {
        CargoStatus status = bookingServiceFacade.loadCargoForTracking(trackingId);
        if (status == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Unknown tracking ID\"}")
                    .build();
        }
        return Response.ok(status).build();
    }
}
