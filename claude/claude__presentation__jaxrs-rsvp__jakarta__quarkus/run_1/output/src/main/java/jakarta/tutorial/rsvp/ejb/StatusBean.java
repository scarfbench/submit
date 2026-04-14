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

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.tutorial.rsvp.entity.Event;
import jakarta.tutorial.rsvp.entity.EventList;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST endpoint for retrieving event status
 * @author ievans
 */
@RequestScoped
@Named
@Path("/status")
public class StatusBean {

    private List<Event> allCurrentEvents;
    private static final Logger logger = Logger.getLogger("jakarta.tutorial.rsvp.ejb.StatusBean");

    @Inject
    EntityManager em;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{eventId}/")
    public Event getEvent(@PathParam("eventId") Long eventId) {
        Event event = em.find(Event.class, eventId);
        return event;
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("all")
    public EventList getAllCurrentEvents() {
        logger.info("Calling getAllCurrentEvents");
        this.allCurrentEvents = (List<Event>)
                em.createNamedQuery("rsvp.entity.Event.getAllUpcomingEvents").getResultList();
        if (this.allCurrentEvents == null) {
            logger.warning("No current events!");
        }
        return new EventList(this.allCurrentEvents);
    }

    public void setAllCurrentEvents(List<Event> events) {
        this.allCurrentEvents = events;
    }
}
