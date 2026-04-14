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
package spring.tutorial.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Spring MVC Controller for the interceptor example
 */
@Controller
public class HelloController {

    @Autowired
    private HelloBean helloBean;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam("name") String name, Model model) {
        helloBean.setName(name);
        String greeting = helloBean.getName();
        model.addAttribute("name", greeting);
        return "response";
    }

    @GetMapping("/back")
    public String back() {
        return "redirect:/";
    }
}
