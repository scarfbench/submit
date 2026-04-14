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
package com.example.interceptor.controller;

import com.example.interceptor.service.HelloBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Spring MVC Controller
 * Replaces JSF managed bean navigation
 */
@Controller
public class HelloController {

    @Autowired
    private HelloBean helloBean;

    /**
     * Display the index page with input form
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("name", helloBean.getName());
        return "index";
    }

    /**
     * Process form submission and display response
     */
    @PostMapping("/submit")
    public String submit(@RequestParam("name") String name, Model model) {
        helloBean.setName(name);
        model.addAttribute("name", helloBean.getName());
        return "response";
    }

    /**
     * Handle back navigation from response page
     */
    @GetMapping("/back")
    public String back() {
        return "redirect:/";
    }

}
