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
package jakarta.tutorial.concurrency.jobs.service;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

/**
 * @author markito
 * Migrated to Quarkus: @Stateless replaced with @ApplicationScoped
 * ManagedExecutorService replaced with standard ExecutorService
 * @EJB replaced with @Inject
 */
@ApplicationScoped
@Path("/JobService")
public class JobService {

    private final static Logger logger = Logger.getLogger(JobService.class.getCanonicalName());
    // http header to check for valid tokens
    private final static String API_TOKEN_HEADER = "X-REST-API-Key";

    // High priority executor with more threads
    private final ExecutorService highPrioExecutor;
    // Low priority executor with fewer threads
    private final ExecutorService lowPrioExecutor;

    @Inject
    TokenStore tokenStore;

    public JobService() {
        // Create thread pools matching the original MES configuration
        // MES_High: coreThreads=5, maxThreads=10
        this.highPrioExecutor = Executors.newFixedThreadPool(10);
        // MES_Low: coreThreads=2, maxThreads=4
        this.lowPrioExecutor = Executors.newFixedThreadPool(4);
    }

    @PreDestroy
    public void cleanup() {
        highPrioExecutor.shutdown();
        lowPrioExecutor.shutdown();
    }

    @GET
    @Path("/token")
    public Response getToken() {
        // static token + dynamic token
        final String token = "123X5-" + UUID.randomUUID().toString();
        tokenStore.put(token);
        return Response.status(200).entity(token).build();
    }

    @POST
    @Path("/process")
    public Response process(final @HeaderParam(API_TOKEN_HEADER) String token,
            final @QueryParam("jobID") int jobID) {

        try {
            if (token != null && tokenStore.isValid(token)) {
                logger.info("Token accepted. Execution with high priority.");
                highPrioExecutor.submit(new JobTask("HIGH-" + jobID));
            } else {
                logger.log(Level.INFO, "Invalid or missing token! {0}", token);
                // requests without token, will be executed but without priority
                lowPrioExecutor.submit(new JobTask("LOW-" + jobID));
            }
        } catch (RejectedExecutionException ree) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Job " + jobID + " NOT submitted. " + ree.getMessage()).build();
        }

        return Response.status(Response.Status.OK).entity("Job " + jobID + " successfully submitted.").build();
    }

    static class JobTask implements Runnable {

        private final String jobID;
        private final int JOB_EXECUTION_TIME= 10000;

        public JobTask(String id) {
            this.jobID = id;
        }

        @Override
        public void run() {
            try {
                logger.log(Level.INFO, "Task started {0}", jobID);
                Thread.sleep(JOB_EXECUTION_TIME); // 10 seconds per job
                logger.log(Level.INFO, "Task finished {0}", jobID);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
            }
        }
    }
}
