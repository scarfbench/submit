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
 * Spring MVC Controller for timer management
 * @author ian
 */
@Controller
public class TimerManager {

    @Autowired
    private TimerSessionBean timerSession;

    /**
     * Display the timer client page
     */
    @GetMapping({"/", "/timer-client"})
    public String showTimerClient(Model model) {
        model.addAttribute("lastProgrammaticTimeout", timerSession.getLastProgrammaticTimeout());
        model.addAttribute("lastAutomaticTimeout", timerSession.getLastAutomaticTimeout());
        return "timer-client";
    }

    /**
     * Handle the set timer action
     */
    @PostMapping("/setTimer")
    public String setTimer(Model model) {
        long timeoutDuration = 8000;
        timerSession.setTimer(timeoutDuration);
        model.addAttribute("lastProgrammaticTimeout", timerSession.getLastProgrammaticTimeout());
        model.addAttribute("lastAutomaticTimeout", timerSession.getLastAutomaticTimeout());
        return "redirect:/timer-client";
    }

}
