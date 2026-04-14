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
package jakarta.tutorial.producermethods;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.WebApplicationContext;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Spring Controller that calls a Coder implementation to perform a transformation on
 * an input string
 */
@Controller
public class CoderBean {

    private String inputString = "";
    private String codedString = "";
    @Max(26)
    @Min(0)
    @NotNull
    private Integer transVal = 0;
    private final static int TEST = 1;
    private final static int SHIFT = 2;
    private int coderType = SHIFT; // default value

    /**
     * Bean method that chooses between two beans based on the coderType
     * value.
     *
     * @return Chosen coder implementation
     */
    @Bean
    @Chosen
    @Scope(value = WebApplicationContext.SCOPE_REQUEST)
    public Coder getCoder() {

        switch (coderType) {
            case TEST:
                return new TestCoderImpl();
            case SHIFT:
                return new CoderImpl();
            default:
                return new CoderImpl();
        }
    }

    @Autowired
    @Chosen
    private Coder coder;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("coderBean", this);
        return "index";
    }

    @PostMapping("/encode")
    public String encodeString(Model model) {
        if (coder != null && inputString != null && transVal != null) {
            setCodedString(coder.codeString(inputString, transVal));
        }
        model.addAttribute("coderBean", this);
        return "index";
    }

    @PostMapping("/reset")
    public String reset(Model model) {
        setInputString("");
        setTransVal(0);
        setCodedString("");
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

    public int getCoderType() {
        return coderType;
    }

    public void setCoderType(int coderType) {
        this.coderType = coderType;
    }
}
