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
package jakarta.tutorial.producermethods;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * REST endpoint that provides access to the Coder functionality
 */
@Path("/coder")
public class CoderResource {

    @Inject
    CoderBean coderBean;

    @POST
    @Path("/encode")
    @Produces(MediaType.APPLICATION_JSON)
    public EncodeResponse encode(EncodeRequest request) {
        coderBean.setCoderType(request.coderType);
        coderBean.setInputString(request.inputString);
        coderBean.setTransVal(request.transVal);
        coderBean.encodeString();

        EncodeResponse response = new EncodeResponse();
        response.codedString = coderBean.getCodedString();
        response.inputString = request.inputString;
        response.transVal = request.transVal;
        response.coderType = request.coderType;
        return response;
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Coder Service is running. Use POST /coder/encode to encode strings.";
    }

    public static class EncodeRequest {
        @NotNull
        public String inputString;

        @Min(0)
        @Max(26)
        @NotNull
        public Integer transVal;

        @NotNull
        public Integer coderType; // 1 = TEST, 2 = SHIFT
    }

    public static class EncodeResponse {
        public String inputString;
        public String codedString;
        public Integer transVal;
        public Integer coderType;
    }
}
