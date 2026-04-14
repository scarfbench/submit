/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.rsvp.web;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.tutorial.rsvp.entity.Event;
import jakarta.tutorial.rsvp.entity.Response;

/**
 *
 * @author ievans
 */
@Component
@SessionScope
public class EventManager implements Serializable {

    private static final long serialVersionUID = -3240069895629955984L;
    private static final Logger logger = Logger.getLogger(EventManager.class.getName());
    protected Event currentEvent;
    private Response currentResponse;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final String baseUri = "http://localhost:8080/jaxrs-rsvp-10-SNAPSHOT/webapi/status/";

    /**
     * Default constructor
     */
    public EventManager() {
        
    }

    /**
     * Get the value of currentEvent
     *
     * @return the value of currentEvent
     */
    public Event getCurrentEvent() {

        return currentEvent;
    }

    /**
     * Set the value of currentEvent
     *
     * @param currentEvent new value of currentEvent
     */
    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }

    /**
     * @return the currentResponse
     */
    public Response getCurrentResponse() {
        return currentResponse;
    }

    /**
     * @param currentResponse the currentResponse to set
     */
    public void setCurrentResponse(Response currentResponse) {
        this.currentResponse = currentResponse;
    }
    
    /**
     * Gets a collection of responses for the current event
     *
     * @return a List of responses
     */
    public List<Response> retrieveEventResponses() {
        if (this.currentEvent == null) {
            logger.log(Level.WARNING, "current event is null");
        }
        logger.log(Level.INFO, "getting responses for {0}", this.currentEvent.getName());
        try {
            Event event = restTemplate.getForObject(
                    baseUri + this.currentEvent.getId().toString(),
                    Event.class);
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

    /**
     * Sets the current event
     *
     * @param event the current event
     * @return a JSF action string
     */
    public String retrieveEventStatus(Event event) {
        this.setCurrentEvent(event);
        return "eventStatus";
    }
    
    /**
     * Sets the current response and sends the navigation case
     * 
     * @param response the response that will be viewed
     * @return the navigation case
     */
    public String viewResponse(Response response) {
        this.currentResponse = response;
        return "viewResponse";
    }

}
