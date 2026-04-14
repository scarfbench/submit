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
import jakarta.ws.rs.core.Response;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.tutorial.timersession.ejb.TimerSessionBean;

/**
 * TimerManager provides REST endpoints for the timer application.
 * Migrated from Jakarta EE JSF to Quarkus REST + Qute.
 *
 * @author ian
 */
@Path("/")
public class TimerManager {

    @Inject
    TimerSessionBean timerSession;

    @Inject
    Template timer;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getTimerPage() {
        return timer.data("lastProgrammaticTimeout", getLastProgrammaticTimeout())
                    .data("lastAutomaticTimeout", getLastAutomaticTimeout());
    }

    @POST
    @Path("/setTimer")
    @Produces(MediaType.TEXT_HTML)
    public Response setTimer() {
        long timeoutDuration = 8000;
        timerSession.setTimer(timeoutDuration);
        // Redirect back to the main page
        return Response.seeOther(java.net.URI.create("/")).build();
    }

    /**
     * @return the lastProgrammaticTimeout
     */
    public String getLastProgrammaticTimeout() {
        return timerSession.getLastProgrammaticTimeout();
    }

    /**
     * @return the lastAutomaticTimeout
     */
    public String getLastAutomaticTimeout() {
        return timerSession.getLastAutomaticTimeout();
    }
}
