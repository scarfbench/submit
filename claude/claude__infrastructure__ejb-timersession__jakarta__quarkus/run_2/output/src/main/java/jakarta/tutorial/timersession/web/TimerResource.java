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
package jakarta.tutorial.timersession.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.tutorial.timersession.ejb.TimerSessionBean;

/**
 * REST endpoint for timer operations.
 * Replaces JSF UI with RESTful API.
 */
@Path("/timer")
public class TimerResource {

    @Inject
    TimerSessionBean timerSession;

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public TimerStatus getStatus() {
        TimerStatus status = new TimerStatus();
        status.lastProgrammaticTimeout = timerSession.getLastProgrammaticTimeout();
        status.lastAutomaticTimeout = timerSession.getLastAutomaticTimeout();
        return status;
    }

    @POST
    @Path("/set")
    @Produces(MediaType.APPLICATION_JSON)
    public TimerResponse setTimer() {
        long timeoutDuration = 8000;
        timerSession.setTimer(timeoutDuration);
        TimerResponse response = new TimerResponse();
        response.message = "Timer set for " + timeoutDuration + " milliseconds";
        response.duration = timeoutDuration;
        return response;
    }

    public static class TimerStatus {
        public String lastProgrammaticTimeout;
        public String lastAutomaticTimeout;
    }

    public static class TimerResponse {
        public String message;
        public long duration;
    }
}
