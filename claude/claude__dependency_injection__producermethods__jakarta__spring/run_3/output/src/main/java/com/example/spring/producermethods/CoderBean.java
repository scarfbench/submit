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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Managed bean that calls a Coder implementation to perform a transformation on
 * an input string
 */
@Component("coderBean")
@RequestScope
public class CoderBean {

    private String inputString;
    private String codedString;
    @Max(26)
    @Min(0)
    @NotNull
    private int transVal;
    private final static int TEST = 1;
    private final static int SHIFT = 2;
    private int coderType = SHIFT; // default value

    @Autowired
    @Chosen
    private Coder coder;

    public void encodeString() {
        setCodedString(coder.codeString(inputString, transVal));
    }

    public void reset() {
        setInputString("");
        setTransVal(0);
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

    public int getTransVal() {
        return transVal;
    }

    public void setTransVal(int tval) {
        transVal = tval;
    }

    public int getCoderType() {
        return coderType;
    }

    public void setCoderType(int coderType) {
        this.coderType = coderType;
    }
}
