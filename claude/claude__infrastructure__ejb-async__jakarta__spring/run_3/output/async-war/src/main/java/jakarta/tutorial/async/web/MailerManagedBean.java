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
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.tutorial.async.ejb.MailerBean;

/**
 * Spring MVC Controller for mail sending
 * Migrated from Jakarta EE JSF Managed Bean
 *
 * @author ievans
 */
@Controller
public class MailerManagedBean {

    @Autowired
    protected MailerBean mailerBean;

    private static final Logger logger = Logger.getLogger(MailerManagedBean.class.getName());
    private Map<String, CompletableFuture<String>> mailStatusMap = new HashMap<>();

    /**
     * Display the mail form
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Send email asynchronously
     */
    @PostMapping("/send")
    @ResponseBody
    public Map<String, String> send(@RequestParam("email") String email) {
        Map<String, String> response = new HashMap<>();
        try {
            CompletableFuture<String> mailStatus = mailerBean.sendMessage(email);
            mailStatusMap.put(email, mailStatus);
            response.put("status", "Processing... (check status endpoint)");
            response.put("message", "Email is being sent to " + email);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            response.put("status", "Error");
            response.put("message", ex.getMessage());
        }
        return response;
    }

    /**
     * Check the status of email sending
     */
    @GetMapping("/status")
    @ResponseBody
    public Map<String, String> getStatus(@RequestParam("email") String email) {
        Map<String, String> response = new HashMap<>();
        CompletableFuture<String> mailStatus = mailStatusMap.get(email);

        if (mailStatus == null) {
            response.put("status", "No email send request found for: " + email);
        } else if (mailStatus.isDone()) {
            try {
                String result = mailStatus.get();
                response.put("status", result);
                mailStatusMap.remove(email);
            } catch (Exception ex) {
                response.put("status", "Error: " + ex.getMessage());
            }
        } else {
            response.put("status", "Processing...");
        }
        return response;
    }

    /**
     * Display response page
     */
    @GetMapping("/response")
    public String response(@RequestParam(value = "email", required = false) String email, Model model) {
        if (email != null) {
            CompletableFuture<String> mailStatus = mailStatusMap.get(email);
            if (mailStatus != null && mailStatus.isDone()) {
                try {
                    model.addAttribute("status", mailStatus.get());
                } catch (Exception ex) {
                    model.addAttribute("status", "Error: " + ex.getMessage());
                }
            } else {
                model.addAttribute("status", "Processing... (refresh to check again)");
            }
        } else {
            model.addAttribute("status", "No email specified");
        }
        return "response";
    }
}
