package io.quarkuscoffeeshop.coffeeshop.web;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * Health check endpoint for the Coffee Shop application.
 */
@Path("/health")
public class HealthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        return Response.ok(Map.of("status", "UP", "application", "coffeeshop-jakarta")).build();
    }
}
