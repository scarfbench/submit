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
package spring.tutorial.rsvp.web;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import spring.tutorial.rsvp.controller.StatusController;
import spring.tutorial.rsvp.entity.Event;
import spring.tutorial.rsvp.entity.Person;
import spring.tutorial.rsvp.util.ResponseEnum;

/**
 * JSF Managed Bean for Status Management
 *
 * @author ievans
 */
@Named
@SessionScope
@Component
public class StatusManager implements Serializable {

    private static final long serialVersionUID = 1;
    private static final Logger logger = Logger.getLogger(StatusManager.class.getName());
    private Event event;
    private List<Event> events;
    private Client client;
    private final String baseUri = "http://localhost:8080/webapi";
    private WebTarget target;

    @Autowired
    private StatusController statusService;

    /**
     * Default constructor creates the JAX-RS client
     */
    public StatusManager() {
        client = ClientBuilder.newClient();
    }

    @PreDestroy
    private void clean() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Sets the event
     *
     * @param event the current event
     * @return a JSF action string
     */
    public String getEventStatus(Event event) {
        if (event == null || event.getResponses() == null)
            return "No responses";
        int size = event.getResponses().size();
        return size == 0 ? "No responses" : size + " response(s)";
    }

    /**
     * Get all the events
     *
     * @return all the events
     */
    public List<Event> getEvents() {
        return statusService.getAllCurrentEvents();
    }

    /**
     * Setter for all the events
     *
     * @param events the events to set
     */
    public void setEvents(List<Event> events) {
        this.events = events;
    }

    /**
     * Retrieve the status values
     *
     * @return an array of response values
     */
    public ResponseEnum[] getStatusValues() {
        return ResponseEnum.values();
    }

    /**
     * Change the status of a user's response
     *
     * @param userResponse the new response
     * @param person       the attendee
     * @param event        the event
     * @return the navigation case
     */
    public String changeStatus(ResponseEnum userResponse, Person person, Event event) {
        String navigation;
        try {
            logger.log(Level.INFO, "changing status to {0} for {1} {2} for event ID {3}.",
                    new Object[] { userResponse, person.getFirstName(), person.getLastName(),
                            event.getId().toString() });
            client.target(baseUri).path(event.getId().toString()).path(person.getId().toString())
                    .request(MediaType.APPLICATION_JSON).post(Entity.xml(userResponse.getLabel()));
            navigation = "changedStatus";
        } catch (ResponseProcessingException ex) {
            logger.log(Level.WARNING, "couldn''t change status for {0} {1}",
                    new Object[] { person.getFirstName(), person.getLastName() });
            logger.log(Level.WARNING, ex.getMessage());
            navigation = "error";
        }
        return navigation;
    }
}
