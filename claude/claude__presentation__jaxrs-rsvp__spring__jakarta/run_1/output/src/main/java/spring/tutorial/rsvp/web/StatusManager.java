package spring.tutorial.rsvp.web;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

import spring.tutorial.rsvp.entity.Event;
import spring.tutorial.rsvp.entity.Person;
import spring.tutorial.rsvp.util.ResponseEnum;

@Named("statusManager")
@SessionScoped
public class StatusManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(StatusManager.class.getName());

    private Event event;
    private List<Event> events;
    private Client client;
    private final String baseUri = "http://localhost:8080/rsvp/webapi";

    public StatusManager() {
        client = ClientBuilder.newClient();
    }

    @PreDestroy
    private void clean() {
        if (client != null) {
            client.close();
        }
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getEventStatus(Event event) {
        this.setEvent(event);
        return "eventStatus";
    }

    public List<Event> getEvents() {
        List<Event> returnedEvents = null;
        try {
            Event[] eventsArray = client.target(baseUri)
                    .path("/status/all")
                    .request(MediaType.APPLICATION_XML)
                    .get(Event[].class);
            if (eventsArray == null) {
                logger.log(Level.SEVERE, "Returned events null.");
            } else {
                returnedEvents = java.util.Arrays.asList(eventsArray);
                logger.log(Level.INFO, "Events have been returned: {0}", returnedEvents.size());
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error retrieving all events.");
            logger.log(Level.SEVERE, "base URI is {0}", baseUri);
            logger.log(Level.SEVERE, "path is {0}", "all");
            logger.log(Level.SEVERE, "Exception is {0}", ex.getMessage());
        }
        return returnedEvents;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public ResponseEnum[] getStatusValues() {
        return ResponseEnum.values();
    }

    public String changeStatus(ResponseEnum userResponse, Person person, Event event) {
        String navigation;
        try {
            logger.log(Level.INFO,
                    "changing status to {0} for {1} {2} for event ID {3}.",
                    new Object[]{userResponse,
                            person.getFirstName(),
                            person.getLastName(),
                            event.getId().toString()});
            client.target(baseUri)
                   .path("/{eventId}/{inviteId}")
                   .resolveTemplate("eventId", event.getId().toString())
                   .resolveTemplate("inviteId", person.getId().toString())
                   .request()
                   .post(Entity.entity(userResponse.getLabel(), MediaType.APPLICATION_XML));
            navigation = "changedStatus";
        } catch (Exception ex) {
            logger.log(Level.WARNING, "couldn't change status for {0} {1}",
                    new Object[]{person.getFirstName(), person.getLastName()});
            logger.log(Level.WARNING, ex.getMessage());
            navigation = "error";
        }
        return navigation;
    }
}
