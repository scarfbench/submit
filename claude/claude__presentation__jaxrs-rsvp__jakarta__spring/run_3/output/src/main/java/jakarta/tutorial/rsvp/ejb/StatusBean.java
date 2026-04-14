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
package jakarta.tutorial.rsvp.ejb;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.tutorial.rsvp.entity.Event;

/**
 *
 * @author ievans
 */
@RestController
@RequestMapping("/webapi/status")
public class StatusBean {

    private List<Event> allCurrentEvents;
    private static final Logger logger = Logger.getLogger("jakarta.tutorial.rsvp.ejb.StatusBean");

    @PersistenceContext
    private EntityManager em;
    
    @GetMapping(value = "/{eventId}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Event getEvent(@PathVariable("eventId") Long eventId) {
        Event event = em.find(Event.class, eventId);
        return event;
    }
    
    @GetMapping(value = "/all", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public List<Event> getAllCurrentEvents() {
        logger.info("Calling getAllCurrentEvents");
        this.allCurrentEvents = (List<Event>)
                em.createNamedQuery("rsvp.entity.Event.getAllUpcomingEvents").getResultList();
        if (this.allCurrentEvents == null) {
            logger.warning("No current events!");
        }
        return this.allCurrentEvents;
    }

    public void setAllCurrentEvents(List<Event> events) {
        this.allCurrentEvents = events;
    }
}
