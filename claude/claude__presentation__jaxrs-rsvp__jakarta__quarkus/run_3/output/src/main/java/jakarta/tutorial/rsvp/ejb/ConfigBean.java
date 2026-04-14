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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.tutorial.rsvp.entity.Event;
import jakarta.tutorial.rsvp.entity.Person;
import jakarta.tutorial.rsvp.entity.Response;
import jakarta.tutorial.rsvp.util.ResponseEnum;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;

/**
 *
 * @author ievans
 */
@ApplicationScoped
public class ConfigBean {

    @Inject
    EntityManager em;

    private static final Logger logger = Logger.getLogger("jakarta.tutorial.rsvp.ejb.ConfigBean");

    @Transactional
    public void init(@Observes StartupEvent event) {
        // create the event owner
        Person dad = new Person();
        dad.setFirstName("Father");
        dad.setLastName("OfJava");
        em.persist(dad);

        // create the event
        Event rsvpEvent = new Event();
        rsvpEvent.setName("Duke's Birthday Party");
        rsvpEvent.setLocation("Top of the Mark");
        Calendar cal = new GregorianCalendar(2010, Calendar.MAY, 23, 19, 0);
        rsvpEvent.setEventDate(cal.getTime());
        em.persist(rsvpEvent);

        // set the relationships
        dad.getOwnedEvents().add(rsvpEvent);
        dad.getEvents().add(rsvpEvent);
        rsvpEvent.setOwner(dad);
        rsvpEvent.getInvitees().add(dad);
        Response dadsResponse = new Response(rsvpEvent, dad, ResponseEnum.ATTENDING);
        em.persist(dadsResponse);
        rsvpEvent.getResponses().add(dadsResponse);

        // create some invitees
        Person duke = new Person();
        duke.setFirstName("Duke");
        duke.setLastName("OfJava");
        em.persist(duke);

        Person tux = new Person();
        tux.setFirstName("Tux");
        tux.setLastName("Penguin");
        em.persist(tux);

        // set the relationships
        rsvpEvent.getInvitees().add(duke);
        duke.getEvents().add(rsvpEvent);
        Response dukesResponse = new Response(rsvpEvent, duke);
        em.persist(dukesResponse);
        rsvpEvent.getResponses().add(dukesResponse);
        duke.getResponses().add(dukesResponse);

        rsvpEvent.getInvitees().add(tux);
        tux.getEvents().add(rsvpEvent);
        Response tuxsResponse = new Response(rsvpEvent, tux);
        em.persist(tuxsResponse);
        rsvpEvent.getResponses().add(tuxsResponse);
        tux.getResponses().add(tuxsResponse);

    }

}
