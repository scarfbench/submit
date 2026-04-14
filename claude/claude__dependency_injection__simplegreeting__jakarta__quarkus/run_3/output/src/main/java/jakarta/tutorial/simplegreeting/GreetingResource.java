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
package jakarta.tutorial.simplegreeting;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@Path("/")
public class GreetingResource {

    @Inject
    @Informal
    Greeting greeting;

    @Inject
    Template index;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("name") String name) {
        String salutation = "";
        if (name != null && !name.trim().isEmpty()) {
            salutation = greeting.greet(name);
        }
        return index.data("name", name)
                    .data("salutation", salutation);
    }

    @GET
    @Path("/greet")
    @Produces(MediaType.APPLICATION_JSON)
    public GreetingResponse greet(@QueryParam("name") String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "World";
        }
        return new GreetingResponse(greeting.greet(name));
    }

    public static class GreetingResponse {
        public String message;

        public GreetingResponse(String message) {
            this.message = message;
        }
    }
}
