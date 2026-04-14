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
package spring.tutorial.rsvp.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import spring.tutorial.rsvp.entity.Response;
import spring.tutorial.rsvp.util.ResponseEnum;

/**
 * REST Controller for managing RSVP responses
 *
 * @author ievans
 */
@RestController
@RequestMapping("/webapi/{eventId}/{inviteId}")
@Transactional
public class ResponseController {

    @PersistenceContext
    private EntityManager em;

    private static final Logger logger = Logger.getLogger(ResponseController.class.getName());

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Response getResponse(@PathVariable Long eventId, @PathVariable Long inviteId) {
        Response response = (Response) em.createNamedQuery("rsvp.entity.Response.findResponseByEventAndPerson")
                .setParameter("eventId", eventId)
                .setParameter("personId", inviteId)
                .getSingleResult();
        return response;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public void putResponse(@RequestBody String userResponse, @PathVariable Long eventId, @PathVariable Long inviteId) {
        logger.log(Level.INFO, "Updating status to {0} for person ID {1} for event ID {2}",
                new Object[] { userResponse, inviteId, eventId });

        Response response = (Response) em.createNamedQuery("rsvp.entity.Response.findResponseByEventAndPerson")
                .setParameter("eventId", eventId)
                .setParameter("personId", inviteId)
                .getSingleResult();

        if (userResponse.equals(ResponseEnum.ATTENDING.getLabel())
                && !response.getResponse().equals(ResponseEnum.ATTENDING)) {
            response.setResponse(ResponseEnum.ATTENDING);
            em.merge(response);
        } else if (userResponse.equals(ResponseEnum.NOT_ATTENDING.getLabel())
                && !response.getResponse().equals(ResponseEnum.NOT_ATTENDING)) {
            response.setResponse(ResponseEnum.NOT_ATTENDING);
            em.merge(response);
        } else if (userResponse.equals(ResponseEnum.MAYBE_ATTENDING.getLabel())
                && !response.getResponse().equals(ResponseEnum.MAYBE_ATTENDING)) {
            response.setResponse(ResponseEnum.MAYBE_ATTENDING);
            em.merge(response);
        }
    }
}
