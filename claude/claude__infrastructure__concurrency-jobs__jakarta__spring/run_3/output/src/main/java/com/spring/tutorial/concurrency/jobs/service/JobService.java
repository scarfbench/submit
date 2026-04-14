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
package com.spring.tutorial.concurrency.jobs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * Job Service REST Controller
 * Migrated from Jakarta EJB/JAX-RS to Spring REST Controller
 *
 * @author markito
 */
@RestController
@RequestMapping("/webapi/JobService")
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);
    private static final String API_TOKEN_HEADER = "X-REST-API-Key";

    @Autowired
    @Qualifier("highPriorityExecutor")
    private Executor highPrioExecutor;

    @Autowired
    @Qualifier("lowPriorityExecutor")
    private Executor lowPrioExecutor;

    @Autowired
    private TokenStore tokenStore;

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
                highPrioExecutor.execute(new JobTask("HIGH-" + jobID));
            } else {
                logger.info("Invalid or missing token! {}", token);
                // requests without token, will be executed but without priority
                lowPrioExecutor.execute(new JobTask("LOW-" + jobID));
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
                logger.info("Task started {}", jobID);
                Thread.sleep(JOB_EXECUTION_TIME);
                logger.info("Task finished {}", jobID);
            } catch (InterruptedException ex) {
                logger.error("Task interrupted", ex);
                Thread.currentThread().interrupt();
            }
        }
    }
}
