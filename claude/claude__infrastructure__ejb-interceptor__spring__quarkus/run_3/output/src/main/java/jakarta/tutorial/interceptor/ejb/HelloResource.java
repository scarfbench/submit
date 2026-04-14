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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * REST endpoint to demonstrate interceptor functionality
 * Replaces JSF interface for Quarkus migration
 */
@Path("/hello")
public class HelloResource {

    @Inject
    HelloBean helloBean;

    @POST
    @Path("/setName")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String setName(String name) {
        helloBean.setName(name);
        return "Name set successfully. The interceptor converted it to lowercase.";
    }

    @GET
    @Path("/getName")
    @Produces(MediaType.TEXT_PLAIN)
    public String getName() {
        String name = helloBean.getName();
        return name != null ? "Hello, " + name + "." : "No name set yet.";
    }
}
