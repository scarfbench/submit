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
package jakarta.tutorial.async.web;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.tutorial.async.ejb.MailerBean;

/**
 *
 * @author ievans
 */
@RestController
@RequestMapping("/api/mail")
public class MailerManagedBean {

    @Autowired
    protected MailerBean mailerBean;

    private static final Logger logger = Logger.getLogger(MailerManagedBean.class.getName());

    private final Map<String, Future<String>> mailStatusMap = new ConcurrentHashMap<>();

    /**
     * Creates a new instance of MailerManagedBean
     */
    public MailerManagedBean() {
    }

    /**
     * Send email endpoint
     */
    @PostMapping("/send")
    public Map<String, String> send(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();
        try {
            Future<String> mailStatus = mailerBean.sendMessage(email);
            mailStatusMap.put(email, mailStatus);
            response.put("status", "Processing");
            response.put("message", "Email is being sent to " + email);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            response.put("status", "Error");
            response.put("message", ex.getMessage());
        }
        return response;
    }

    /**
     * Check status endpoint
     */
    @GetMapping("/status")
    public Map<String, String> getStatus(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();
        Future<String> mailStatus = mailStatusMap.get(email);

        if (mailStatus == null) {
            response.put("status", "Unknown");
            response.put("message", "No email found for " + email);
        } else if (mailStatus.isDone()) {
            try {
                String result = mailStatus.get();
                response.put("status", result);
                response.put("message", "Email sent successfully");
                mailStatusMap.remove(email);
            } catch (ExecutionException | CancellationException | InterruptedException ex) {
                response.put("status", "Error");
                response.put("message", ex.getCause() != null ? ex.getCause().toString() : ex.getMessage());
            }
        } else {
            response.put("status", "Processing");
            response.put("message", "Email is still being sent");
        }
        return response;
    }
}
