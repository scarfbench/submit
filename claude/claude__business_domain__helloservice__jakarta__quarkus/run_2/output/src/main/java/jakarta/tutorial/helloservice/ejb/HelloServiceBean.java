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
package jakarta.tutorial.helloservice.ejb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * HelloServiceBean is a REST service endpoint implemented as an application-scoped bean.
 * Migrated from EJB Stateless + JAX-WS to Quarkus REST/JAX-RS.
 */

@ApplicationScoped
@Path("/hello")
public class HelloServiceBean {
    private final String message = "Hello, ";

    public HelloServiceBean() {}

    @GET
    @Path("/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello(@PathParam("name") String name) {
        return message + name + ".";
    }
}
