package org.example.realworldapi.application.web.resource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/health")
public class HealthResource {

  private static final String UP_JSON = "{\"status\":\"UP\"}";

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response health() {
    return Response.ok(UP_JSON).type(MediaType.APPLICATION_JSON).build();
  }

  @GET
  @Path("/live")
  @Produces(MediaType.APPLICATION_JSON)
  public Response live() {
    return Response.ok(UP_JSON).type(MediaType.APPLICATION_JSON).build();
  }

  @GET
  @Path("/ready")
  @Produces(MediaType.APPLICATION_JSON)
  public Response ready() {
    return Response.ok(UP_JSON).type(MediaType.APPLICATION_JSON).build();
  }
}
