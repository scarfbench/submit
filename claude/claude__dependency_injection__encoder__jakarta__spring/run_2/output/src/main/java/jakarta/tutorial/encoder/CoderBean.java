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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Spring MVC controller that calls a Coder implementation to perform a transformation
 * on an input string
 */
@Controller
public class CoderBean {

    @Autowired
    private Coder coder;

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("coderForm", new CoderForm());
        return "index";
    }

    @PostMapping("/encode")
    public String encodeString(@Valid @ModelAttribute("coderForm") CoderForm form,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "index";
        }
        String encoded = coder.codeString(form.getInputString(), form.getTransVal());
        form.setCodedString(encoded);
        model.addAttribute("coderForm", form);
        return "index";
    }

    @PostMapping("/reset")
    public String reset(Model model) {
        model.addAttribute("coderForm", new CoderForm());
        return "index";
    }

    /**
     * Form object for binding input data
     */
    public static class CoderForm {
        private String inputString = "";
        private String codedString = "";

        @Max(value = 26, message = "Value must be between 0 and 26")
        @Min(value = 0, message = "Value must be between 0 and 26")
        @NotNull(message = "Value is required")
        private Integer transVal = 0;

        public String getInputString() {
            return inputString;
        }

        public void setInputString(String inputString) {
            this.inputString = inputString;
        }

        public String getCodedString() {
            return codedString;
        }

        public void setCodedString(String codedString) {
            this.codedString = codedString;
        }

        public Integer getTransVal() {
            return transVal;
        }

        public void setTransVal(Integer transVal) {
            this.transVal = transVal;
        }
    }
}
