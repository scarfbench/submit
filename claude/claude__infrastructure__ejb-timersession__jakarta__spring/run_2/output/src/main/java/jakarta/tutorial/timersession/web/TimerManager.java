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

import jakarta.tutorial.timersession.ejb.TimerSessionBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Spring MVC Controller for Timer Management
 * Migrated from Jakarta EE CDI Managed Bean to Spring Controller
 *
 * @author ian
 */
@Controller
public class TimerManager {

    private final TimerSessionBean timerSession;

    /** Creates a new instance of TimerManager */
    public TimerManager(TimerSessionBean timerSession) {
        this.timerSession = timerSession;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("lastProgrammaticTimeout", timerSession.getLastProgrammaticTimeout());
        model.addAttribute("lastAutomaticTimeout", timerSession.getLastAutomaticTimeout());
        return "timer-client";
    }

    @PostMapping("/setTimer")
    public String setTimer(Model model) {
        long timeoutDuration = 8000;
        timerSession.setTimer(timeoutDuration);
        model.addAttribute("lastProgrammaticTimeout", timerSession.getLastProgrammaticTimeout());
        model.addAttribute("lastAutomaticTimeout", timerSession.getLastAutomaticTimeout());
        return "redirect:/";
    }
}
