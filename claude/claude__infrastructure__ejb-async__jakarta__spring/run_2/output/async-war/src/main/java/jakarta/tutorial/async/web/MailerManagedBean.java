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

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import jakarta.tutorial.async.ejb.MailerBean;

/**
 *
 * @author ievans
 */
@Controller
@SessionAttributes({"email", "status", "mailStatus"})
public class MailerManagedBean {

    @Autowired
    protected MailerBean mailerBean;

    private static final Logger logger = Logger.getLogger(MailerManagedBean.class.getName());

    /**
     * Creates a new instance of MailerManagedBean
     */
    public MailerManagedBean() {
    }

    @GetMapping("/")
    public String index(Model model) {
        if (!model.containsAttribute("email")) {
            model.addAttribute("email", "");
        }
        if (!model.containsAttribute("status")) {
            model.addAttribute("status", "");
        }
        return "index";
    }

    @PostMapping("/send")
    public String send(@RequestParam String email, Model model) {
        try {
            CompletableFuture<String> mailStatus = mailerBean.sendMessage(email);
            model.addAttribute("email", email);
            model.addAttribute("status", "Processing... (refresh to check again)");
            model.addAttribute("mailStatus", mailStatus);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            model.addAttribute("status", "Error: " + ex.getMessage());
        }
        return "redirect:/response";
    }

    @GetMapping("/response")
    public String response(Model model) {
        if (model.containsAttribute("mailStatus")) {
            CompletableFuture<String> mailStatus = (CompletableFuture<String>) model.getAttribute("mailStatus");
            if (mailStatus != null && mailStatus.isDone()) {
                try {
                    String status = mailStatus.get();
                    model.addAttribute("status", status);
                } catch (ExecutionException | CancellationException | InterruptedException ex) {
                    model.addAttribute("status", "Error: " + ex.getCause().toString());
                }
            }
        }
        return "response";
    }

}
