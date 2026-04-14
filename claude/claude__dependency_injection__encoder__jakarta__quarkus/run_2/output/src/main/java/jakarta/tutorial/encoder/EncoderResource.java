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
package jakarta.tutorial.encoder;

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
 * REST resource that exposes the encoder functionality
 */
@Path("/encoder")
public class EncoderResource {

    @Inject
    CoderBean coderBean;

    @GET
    @Path("/encode")
    @Produces(MediaType.TEXT_PLAIN)
    public String encode(
            @QueryParam("input") @NotNull String inputString,
            @QueryParam("shift") @NotNull @Min(0) @Max(26) Integer transVal) {
        return coderBean.encodeString(inputString, transVal);
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Encoder service is running";
    }
}
