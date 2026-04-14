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
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webapi/JobService")
public class JobService {

    private static final Logger logger = Logger.getLogger(JobService.class.getCanonicalName());
    private static final String API_TOKEN_HEADER = "X-REST-API-Key";

    @Autowired
    @High
    private Executor highPrioExecutor;

    @Autowired
    @Low
    private Executor lowPrioExecutor;

    @Autowired
    private TokenStore tokenStore;

    @GetMapping("/token")
    public ResponseEntity<String> getToken() {
        final String token = "123X5-" + UUID.randomUUID();
        tokenStore.put(token);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/process")
    public ResponseEntity<String> process(@RequestHeader(name = API_TOKEN_HEADER, required = false) String token,
                                          @RequestParam("jobID") int jobID) {
        try {
            if (token != null && tokenStore.isValid(token)) {
                logger.info("Token accepted. Execution with high priority.");
                highPrioExecutor.execute(new JobTask("HIGH-" + jobID));
            } else {
                logger.log(Level.INFO, "Invalid or missing token! {0}", token);
                lowPrioExecutor.execute(new JobTask("LOW-" + jobID));
            }
        } catch (RejectedExecutionException ree) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Job " + jobID + " NOT submitted. " + ree.getMessage());
        }
        return ResponseEntity.ok("Job " + jobID + " successfully submitted.");
    }

    static class JobTask implements Runnable {
        private static final Logger logger = Logger.getLogger(JobTask.class.getName());
        private final String jobID;
        private final int JOB_EXECUTION_TIME = 10_000;

        JobTask(String id) { this.jobID = id; }

        @Override public void run() {
            try {
                logger.log(Level.INFO, "Task started {0}", jobID);
                Thread.sleep(JOB_EXECUTION_TIME);
                logger.log(Level.INFO, "Task finished {0}", jobID);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
            }
        }
    }
}
