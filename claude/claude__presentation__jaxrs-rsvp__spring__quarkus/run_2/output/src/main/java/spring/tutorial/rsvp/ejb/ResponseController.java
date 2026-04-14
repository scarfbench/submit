package spring.tutorial.rsvp.ejb;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import spring.tutorial.rsvp.util.ResponseEnum;
import spring.tutorial.rsvp.entity.Response;

@ApplicationScoped
@Path("/webapi/{eventId}/{inviteId}")
public class ResponseController {

    @Inject
    EntityManager em;

    private static final Logger logger = Logger.getLogger(ResponseController.class.getName());

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getResponse(@PathParam("eventId") Long eventId,
                                @PathParam("inviteId") Long inviteId) {
        return (Response) em.createNamedQuery("rsvp.entity.Response.findResponseByEventAndPerson")
                .setParameter("eventId", eventId)
                .setParameter("personId", inviteId)
                .getSingleResult();
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    public void putResponse(String userResponse,
                            @PathParam("eventId") Long eventId,
                            @PathParam("inviteId") Long inviteId) {

        logger.log(Level.INFO,
                "Updating status to {0} for person ID {1} for event ID {2}",
                new Object[]{userResponse, inviteId, eventId});

        Response response = (Response) em.createNamedQuery("rsvp.entity.Response.findResponseByEventAndPerson")
                .setParameter("eventId", eventId)
                .setParameter("personId", inviteId)
                .getSingleResult();

        if (userResponse.equals(ResponseEnum.ATTENDING.getLabel())
                && response.getResponse() != ResponseEnum.ATTENDING) {
            response.setResponse(ResponseEnum.ATTENDING);
            em.merge(response);

        } else if (userResponse.equals(ResponseEnum.NOT_ATTENDING.getLabel())
                && response.getResponse() != ResponseEnum.NOT_ATTENDING) {
            response.setResponse(ResponseEnum.NOT_ATTENDING);
            em.merge(response);

        } else if (userResponse.equals(ResponseEnum.MAYBE_ATTENDING.getLabel())
                && response.getResponse() != ResponseEnum.MAYBE_ATTENDING) {
            response.setResponse(ResponseEnum.MAYBE_ATTENDING);
            em.merge(response);
        }
    }
}
