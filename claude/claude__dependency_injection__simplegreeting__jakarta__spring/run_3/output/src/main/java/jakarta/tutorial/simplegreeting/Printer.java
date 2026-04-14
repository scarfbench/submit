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
package jakarta.tutorial.simplegreeting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class Printer {

    @Autowired
    @Informal
    private Greeting greeting;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/greet")
    public String createSalutation(@RequestParam("name") String name, Model model) {
        String salutation = greeting.greet(name);
        model.addAttribute("name", name);
        model.addAttribute("salutation", salutation);
        return "index";
    }
}
