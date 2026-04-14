package spring.tutorial.rsvp.ejb;

import java.util.List;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import spring.tutorial.rsvp.entity.Event;

@Path("/webapi/status")
public class StatusController {

    private static final Logger logger =
            Logger.getLogger(StatusController.class.getName());

    @PersistenceContext
    private EntityManager em;

    private List<Event> allCurrentEvents;

    @GET
    @Path("/{eventId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Event getEvent(@PathParam("eventId") Long eventId) {
        return em.find(Event.class, eventId);
    }

    @GET
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Event> getAllCurrentEvents() {
        logger.info("Calling getAllCurrentEvents");
        allCurrentEvents = em.createNamedQuery("rsvp.entity.Event.getAllUpcomingEvents", Event.class)
                             .getResultList();
        if (allCurrentEvents == null || allCurrentEvents.isEmpty()) {
            logger.warning("No current events!");
        }
        return allCurrentEvents;
    }

    public void setAllCurrentEvents(List<Event> events) {
        this.allCurrentEvents = events;
    }
}
