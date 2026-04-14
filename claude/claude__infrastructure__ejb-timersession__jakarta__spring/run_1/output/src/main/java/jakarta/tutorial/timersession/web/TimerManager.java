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
package jakarta.tutorial.timersession.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.tutorial.timersession.ejb.TimerSessionBean;

/**
 * TimerManager is a Spring MVC Controller that manages timer interactions.
 * Migrated from Jakarta EE JSF Managed Bean to Spring MVC Controller.
 *
 * @author ian
 */
@Controller
public class TimerManager {

    @Autowired
    private TimerSessionBean timerSession;

    /**
     * Display the timer client page
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("lastProgrammaticTimeout", timerSession.getLastProgrammaticTimeout());
        model.addAttribute("lastAutomaticTimeout", timerSession.getLastAutomaticTimeout());
        return "timer-client";
    }

    /**
     * Handle the "Set Timer" button action
     */
    @PostMapping("/setTimer")
    public String setTimer(Model model) {
        long timeoutDuration = 8000;
        timerSession.setTimer(timeoutDuration);

        // Redirect to the index page to show updated values
        return "redirect:/";
    }

    /**
     * Handle the "Refresh" button action
     */
    @PostMapping("/refresh")
    public String refresh(Model model) {
        // Simply redirect to the index page to refresh the view
        return "redirect:/";
    }
}
