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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import jakarta.tutorial.rsvp.entity.Event;
import jakarta.tutorial.rsvp.entity.Person;
import jakarta.tutorial.rsvp.util.ResponseEnum;

/**
 *
 * @author ievans
 */
@Component
@SessionScope
public class StatusManager implements Serializable {

    private static final long serialVersionUID = 1;
    private static final Logger logger = Logger.getLogger(StatusManager.class.getName());
    private Event event;
    private List<Event> events;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private final String baseUri = "http://localhost:8080/jaxrs-rsvp-10-SNAPSHOT/webapi";

    /**
     * Default constructor
     */
    public StatusManager() {
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
        this.setEvent(event);
        return "eventStatus";
    }

    /**
     * Get all the events
     *
     * @return all the events
     */
    public List<Event> getEvents() {
        List<Event> returnedEvents = null;
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUri).build();
            returnedEvents = webClient.get()
                    .uri("/status/all")
                    .accept(MediaType.APPLICATION_XML)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Event>>() {
                    })
                    .block();
            if (returnedEvents == null) {
                logger.log(Level.SEVERE, "Returned events null.");
            } else {
                logger.log(Level.INFO, "Events have been returned.");
            }
        } catch (ResponseStatusException ex) {
            logger.log(Level.SEVERE, "ResponseStatusException thrown.");
            logger.log(Level.SEVERE, "Error is {0}", ex.getMessage());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error retrieving all events.");
            logger.log(Level.SEVERE, "base URI is {0}", baseUri);
            logger.log(Level.SEVERE, "path is {0}", "all");
            logger.log(Level.SEVERE, "Exception is {0}", ex.getMessage());
        }
        return returnedEvents;
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
     * @param person the attendee
     * @param event the event
     * @return the navigation case
     */
    public String changeStatus(ResponseEnum userResponse, Person person, Event event) {
        String navigation;
        try {
            logger.log(Level.INFO,
                    "changing status to {0} for {1} {2} for event ID {3}.",
                    new Object[]{userResponse,
                        person.getFirstName(),
                        person.getLastName(),
                        event.getId().toString()});
            WebClient webClient = webClientBuilder.baseUrl(baseUri).build();
            webClient.post()
                    .uri("/" + event.getId().toString() + "/" + person.getId().toString())
                    .contentType(MediaType.APPLICATION_XML)
                    .body(BodyInserters.fromValue(userResponse.getLabel()))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            navigation = "changedStatus";
        } catch (Exception ex) {
            logger.log(Level.WARNING, "couldn''t change status for {0} {1}",
                    new Object[]{person.getFirstName(),
                        person.getLastName()});
            logger.log(Level.WARNING, ex.getMessage());
            navigation = "error";
        }
        return navigation;
    }
}
