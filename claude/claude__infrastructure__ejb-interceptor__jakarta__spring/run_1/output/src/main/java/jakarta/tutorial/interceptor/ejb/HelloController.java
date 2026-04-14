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
package jakarta.tutorial.interceptor.ejb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Spring MVC Controller
 */
@Controller
public class HelloController {

    @Autowired
    private HelloBean helloBean;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("name", helloBean.getName() != null ? helloBean.getName() : "");
        return "index";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam("name") String name) {
        helloBean.setName(name);
        return "redirect:/response";
    }

    @GetMapping("/response")
    public String response(Model model) {
        model.addAttribute("name", helloBean.getName());
        return "response";
    }

    @GetMapping("/index")
    public String backToIndex() {
        return "redirect:/";
    }

}
