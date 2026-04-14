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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

/**
 * REST Resource for Interceptor Example
 * Replaces JSF backing bean with REST endpoints
 *
 * @author Quarkus Migration
 */
@Path("/")
public class HelloResource {

    @Inject
    HelloBean helloBean;

    @Inject
    Template index;

    @Inject
    Template response;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getIndex() {
        return index.data("name", "");
    }

    @POST
    @Path("/submit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance submitName(@FormParam("name") String name) {
        helloBean.setName(name);
        return response.data("name", helloBean.getName());
    }

    @GET
    @Path("/api/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHello() {
        return Response.ok()
            .entity("{\"name\": \"" + (helloBean.getName() != null ? helloBean.getName() : "") + "\"}")
            .build();
    }
}
