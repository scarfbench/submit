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
package jakarta.tutorial.concurrency.jobs.client;

import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import java.net.URI;

/**
 * Client REST endpoint to test the JobService
 * Migrated from JSF-based client to REST API
 *
 * @author markito
 */
@ApplicationScoped
@Path("/JobClient")
public class JobClient {
    private final static Logger logger = Logger.getLogger(JobClient.class.getCanonicalName());

    private final String serviceEndpoint = "http://localhost:9080/webapi/JobService/process";

    /**
     * Submit a job with token
     * GET /webapi/JobClient/submit?jobID=123&token=xxx
     */
    @GET
    @Path("/submit")
    public Response submit(@QueryParam("jobID") int jobID, @QueryParam("token") String token) {
        try {
            jakarta.ws.rs.client.Client client = jakarta.ws.rs.client.ClientBuilder.newClient();

            Response response = client.target(serviceEndpoint)
                    .queryParam("jobID", jobID)
                    .request()
                    .header("X-REST-API-Key", token)
                    .post(jakarta.ws.rs.client.Entity.text(""));

            String message;
            if (response.getStatus() == 200) {
                message = String.format("Job %d successfully submitted", jobID);
                logger.info(message);
                return Response.ok(message).build();
            } else {
                message = String.format("Job %d was NOT submitted. Status: %d", jobID, response.getStatus());
                logger.warning(message);
                return Response.status(response.getStatus()).entity(message).build();
            }
        } catch (Exception e) {
            String errorMsg = String.format("Error submitting job %d: %s", jobID, e.getMessage());
            logger.severe(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
        }
    }

    /**
     * Submit a job without token (low priority)
     * POST /webapi/JobClient/submitLowPriority?jobID=123
     */
    @POST
    @Path("/submitLowPriority")
    public Response submitLowPriority(@QueryParam("jobID") int jobID) {
        return submit(jobID, null);
    }
}
