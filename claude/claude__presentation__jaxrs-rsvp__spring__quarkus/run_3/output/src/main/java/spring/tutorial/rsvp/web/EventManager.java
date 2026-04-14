package spring.tutorial.rsvp.web;

import spring.tutorial.rsvp.entity.Event;
import spring.tutorial.rsvp.entity.Response;
import spring.tutorial.rsvp.client.StatusRestClient;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@SessionScoped
public class EventManager implements Serializable {

    private static final long serialVersionUID = -3240069895629955984L;
    private static final Logger logger = Logger.getLogger(EventManager.class.getName());

    protected Event currentEvent;
    private Response currentResponse;

    @Inject
    @RestClient
    StatusRestClient client;

    public EventManager() {
        // Default constructor
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }

    public Response getCurrentResponse() {
        return currentResponse;
    }

    public void setCurrentResponse(Response currentResponse) {
        this.currentResponse = currentResponse;
    }

    public List<Response> retrieveEventResponses() {
        if (this.currentEvent == null) {
            logger.log(Level.WARNING, "current event is null");
            return null;
        }

        logger.log(Level.INFO, "getting responses for {0}", this.currentEvent.getName());
        try {
            Event event = client.getEvent(this.currentEvent.getId());
            if (event == null) {
                logger.log(Level.WARNING, "returned event is null");
                return null;
            } else {
                return event.getResponses();
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "an error occurred when getting event responses.");
            return null;
        }
    }

    public String retrieveEventStatus(Event event) {
        this.setCurrentEvent(event);
        return "eventStatus";
    }

    public String viewResponse(Response response) {
        this.currentResponse = response;
        return "viewResponse";
    }
}
