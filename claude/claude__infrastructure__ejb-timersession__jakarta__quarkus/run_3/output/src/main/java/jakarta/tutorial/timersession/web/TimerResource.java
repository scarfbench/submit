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

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST Resource for managing timers through a web interface.
 * Replaces the JSF interface with Qute templates.
 */
@Path("/")
public class TimerResource {

    @Inject
    TimerManager timerManager;

    @Inject
    Template index;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index
                .data("lastProgrammaticTimeout", timerManager.getLastProgrammaticTimeout())
                .data("lastAutomaticTimeout", timerManager.getLastAutomaticTimeout());
    }

    @POST
    @Path("/set-timer")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance setTimer() {
        timerManager.setTimer();
        return index
                .data("lastProgrammaticTimeout", timerManager.getLastProgrammaticTimeout())
                .data("lastAutomaticTimeout", timerManager.getLastAutomaticTimeout())
                .data("message", "Timer set successfully!");
    }
}
