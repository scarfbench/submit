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
package com.spring.tutorial.concurrency.jobs.controller;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web Controller for Job Submission
 * Migrated from Jakarta JSF Backing Bean to Spring MVC Controller
 *
 * @author markito
 */
@Controller
public class JobController {
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    private final String serviceEndpoint = "http://localhost:9080/jobs/webapi/JobService/process";

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("jobID", 0);
        model.addAttribute("token", "");
        return "index";
    }

    @PostMapping("/submit")
    public String submit(
            @RequestParam(value = "jobID", defaultValue = "0") int jobID,
            @RequestParam(value = "token", defaultValue = "") String token,
            RedirectAttributes redirectAttributes) {

        final Client client = ClientBuilder.newClient();

        try {
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

            redirectAttributes.addFlashAttribute("message", message);
            redirectAttributes.addFlashAttribute("messageType", messageType);
            logger.info(message);

            response.close();
        } catch (Exception e) {
            logger.error("Error submitting job", e);
            redirectAttributes.addFlashAttribute("message", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        } finally {
            client.close();
        }

        return "redirect:/";
    }
}
