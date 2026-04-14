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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client to REST service
 *
 * @author markito
 */
@Controller
public class JobClient {
    private final static Logger logger = Logger.getLogger(JobClient.class.getCanonicalName());

    private final String serviceEndpoint = "http://localhost:8080/webapi/JobService/process";

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam(required = false) String token,
                         @RequestParam int jobID,
                         Model model) {
        final Client client = ClientBuilder.newClient();

        final Response response = client.target(serviceEndpoint)
                .queryParam("jobID", jobID)
                .request()
                .header("X-REST-API-Key", token)
                .post(null);

        String message;
        String messageType;
        if (response.getStatus() == 200) {
            message = String.format("Job %d successfully submitted", jobID);
            messageType = "success";
        } else {
            message = String.format("Job %d was NOT submitted", jobID);
            messageType = "error";
        }

        model.addAttribute("message", message);
        model.addAttribute("messageType", messageType);
        logger.info(message);

        return "index";
    }
}
