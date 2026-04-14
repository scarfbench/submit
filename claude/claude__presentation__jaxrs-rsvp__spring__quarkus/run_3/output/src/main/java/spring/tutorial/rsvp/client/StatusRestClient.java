package spring.tutorial.rsvp.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import spring.tutorial.rsvp.entity.Event;
import spring.tutorial.rsvp.entity.Response;

@RegisterRestClient(baseUri = "http://localhost:8080/webapi")
public interface StatusRestClient {

    @GET
    @Path("/status/all")
    @Produces(MediaType.APPLICATION_XML)
    Event[] getAllEvents();

    @GET
    @Path("/status/{eventId}")
    @Produces(MediaType.APPLICATION_XML)
    Event getEvent(@PathParam("eventId") Long eventId);

    @POST
    @Path("/{eventId}/{inviteId}")
    @Consumes(MediaType.APPLICATION_XML)
    void updateResponse(@PathParam("eventId") Long eventId,
                       @PathParam("inviteId") Long inviteId,
                       String response);

    @GET
    @Path("/{eventId}/{inviteId}")
    @Produces(MediaType.APPLICATION_XML)
    Response getResponse(@PathParam("eventId") Long eventId,
                        @PathParam("inviteId") Long inviteId);
}
