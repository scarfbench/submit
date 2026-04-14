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
package jakarta.tutorial.decorators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

/**
 * Spring MVC controller for the Coder application
 * (replaces JSF managed bean interaction)
 */
@Controller
public class CoderController {

    @Autowired
    private CoderBean coderBean;

    /**
     * Show the main form
     */
    @GetMapping({"/", "/index"})
    public String showForm(Model model) {
        model.addAttribute("coderBean", coderBean);
        return "index";
    }

    /**
     * Handle encode action
     */
    @PostMapping("/encode")
    public String encodeString(@Valid @ModelAttribute("coderBean") CoderBean bean,
                               BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        // Copy input values from form bean to session bean
        coderBean.setInputString(bean.getInputString());
        coderBean.setTransVal(bean.getTransVal());

        // Perform encoding
        coderBean.encodeString();

        // Update model with results
        model.addAttribute("coderBean", coderBean);
        return "index";
    }

    /**
     * Handle reset action
     */
    @PostMapping("/reset")
    public String reset(Model model) {
        coderBean.reset();
        model.addAttribute("coderBean", coderBean);
        return "index";
    }
}
