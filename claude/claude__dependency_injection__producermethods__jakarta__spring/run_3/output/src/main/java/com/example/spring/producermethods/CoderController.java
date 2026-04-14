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
package com.example.spring.producermethods;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

/**
 * Spring MVC Controller for String Encoder application
 */
@Controller
public class CoderController {

    @Autowired
    private CoderBean coderBean;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("coderBean", coderBean);
        return "index";
    }

    @PostMapping("/encode")
    public String encode(@Valid @ModelAttribute("coderBean") CoderBean formBean,
                        BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        // Update the bean with form values
        coderBean.setInputString(formBean.getInputString());
        coderBean.setTransVal(formBean.getTransVal());
        coderBean.setCoderType(formBean.getCoderType());

        // Perform encoding
        coderBean.encodeString();

        model.addAttribute("coderBean", coderBean);
        return "index";
    }

    @PostMapping("/reset")
    public String reset(Model model) {
        coderBean.reset();
        model.addAttribute("coderBean", coderBean);
        return "index";
    }
}
