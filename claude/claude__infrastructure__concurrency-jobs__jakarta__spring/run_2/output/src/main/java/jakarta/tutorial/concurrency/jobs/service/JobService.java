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
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for job processing - migrated from JAX-RS/EJB to Spring
 * @author markito
 */
@RestController
@RequestMapping("/webapi/JobService")
public class JobService {

    private final static Logger logger = Logger.getLogger(JobService.class.getCanonicalName());
    // http header to check for valid tokens
    private final static String API_TOKEN_HEADER = "X-REST-API-Key";

    private final ThreadPoolTaskExecutor highPrioExecutor;
    private final ThreadPoolTaskExecutor lowPrioExecutor;
    private final TokenStore tokenStore;

    @Autowired
    public JobService(
            @Qualifier("highPriorityExecutor") ThreadPoolTaskExecutor highPrioExecutor,
            @Qualifier("lowPriorityExecutor") ThreadPoolTaskExecutor lowPrioExecutor,
            TokenStore tokenStore) {
        this.highPrioExecutor = highPrioExecutor;
        this.lowPrioExecutor = lowPrioExecutor;
        this.tokenStore = tokenStore;
    }

    @GetMapping("/token")
    public ResponseEntity<String> getToken() {
        // static token + dynamic token
        final String token = "123X5-" + UUID.randomUUID().toString();
        tokenStore.put(token);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/process")
    public ResponseEntity<String> process(
            @RequestHeader(value = API_TOKEN_HEADER, required = false) String token,
            @RequestParam("jobID") int jobID) {

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
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Job " + jobID + " NOT submitted. " + ree.getMessage());
        }

        return ResponseEntity.ok("Job " + jobID + " successfully submitted.");
    }

    static class JobTask implements Runnable {

        private final String jobID;
        private final int JOB_EXECUTION_TIME = 10000;

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
