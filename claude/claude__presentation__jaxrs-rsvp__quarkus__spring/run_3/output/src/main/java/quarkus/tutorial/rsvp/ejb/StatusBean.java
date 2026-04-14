package quarkus.tutorial.rsvp.ejb;

import java.util.List;
import java.util.logging.Logger;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import quarkus.tutorial.rsvp.entity.Event;

@RestController
@RequestMapping("/webapi/status")
@Transactional
public class StatusBean {

    private static final Logger logger =
        Logger.getLogger("quarkus.tutorial.rsvp.ejb.StatusBean");

    @PersistenceContext
    private EntityManager em;

    @GetMapping("/{eventId}/")
    public Event getEvent(@PathVariable("eventId") Long eventId) {
        Event event = em.find(Event.class, eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event " + eventId + " not found");
        }
        Hibernate.initialize(event.getResponses());
        return event;
    }

    @GetMapping("/all")
    public List<Event> getAllCurrentEvents() {
        logger.info("Calling getAllCurrentEvents");
        @SuppressWarnings("unchecked")
        List<Event> events = (List<Event>) em
            .createNamedQuery("rsvp.entity.Event.getAllUpcomingEvents")
            .getResultList();

        if (events == null || events.isEmpty()) {
            logger.warning("No current events!");
            return events;
        }

        events.forEach(e -> Hibernate.initialize(e.getResponses()));
        return events;
    }

    public void setAllCurrentEvents(List<Event> events) {
    }
}
