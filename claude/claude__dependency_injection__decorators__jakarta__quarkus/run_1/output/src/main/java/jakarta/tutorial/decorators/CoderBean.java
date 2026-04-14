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
package jakarta.tutorial.decorators;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * REST endpoint that calls a Coder implementation to perform a transformation
 * on an input string
 */
@Path("/coder")
public class CoderBean {

    @Inject
    Coder coder;

    @GET
    @Path("/encode")
    @Produces(MediaType.TEXT_PLAIN)
    @Logged
    public String encodeString(
            @QueryParam("input") String inputString,
            @QueryParam("shift") @Max(26) @Min(0) @NotNull Integer transVal) {
        if (inputString == null || inputString.isEmpty()) {
            return "Error: input parameter is required";
        }
        if (transVal == null) {
            return "Error: shift parameter is required";
        }
        return coder.codeString(inputString, transVal);
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Coder service is running. Use /coder/encode?input=text&shift=3 to encode text.";
    }
}
