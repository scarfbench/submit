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
package jakarta.tutorial.interceptor.ejb;

import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

/**
 * REST Resource to handle web interactions
 * Replacement for JSF pages
 */
@Path("/")
public class HelloResource {

    @Inject
    Template index;

    @Inject
    Template response;

    @Inject
    HelloBean helloBean;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        return index.instance();
    }

    @POST
    @Path("/submit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance submit(@FormParam("name") String name) {
        helloBean.setName(name);
        return response.data("name", helloBean.getName());
    }

    @GET
    @Path("/response")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance response() {
        return response.data("name", helloBean.getName());
    }
}
