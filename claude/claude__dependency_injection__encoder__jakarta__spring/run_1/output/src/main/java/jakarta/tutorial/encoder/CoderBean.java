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
package jakarta.tutorial.encoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Spring MVC Controller that calls a Coder implementation to perform a transformation
 * on an input string
 */
@Controller
public class CoderBean {

    private String inputString = "";
    private String codedString = "";
    @Max(26)
    @Min(0)
    @NotNull
    private Integer transVal = 0;

    @Autowired
    private Coder coder;

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("coderBean", this);
        return "index";
    }

    @PostMapping("/encode")
    public String encodeString(@Valid @ModelAttribute CoderBean formData,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("coderBean", formData);
            return "index";
        }

        this.inputString = formData.getInputString();
        this.transVal = formData.getTransVal();
        this.codedString = coder.codeString(inputString, transVal);

        model.addAttribute("coderBean", this);
        return "index";
    }

    @PostMapping("/reset")
    public String reset(Model model) {
        this.inputString = "";
        this.codedString = "";
        this.transVal = 0;

        model.addAttribute("coderBean", this);
        return "index";
    }

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inString) {
        inputString = inString;
    }

    public String getCodedString() {
        return codedString;
    }

    public void setCodedString(String str) {
        codedString = str;
    }

    public Integer getTransVal() {
        return transVal;
    }

    public void setTransVal(Integer tval) {
        transVal = tval;
    }
}
