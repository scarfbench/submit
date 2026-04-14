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
package com.example.interceptor.service;

import org.springframework.stereotype.Component;

/**
 * Migrated from Jakarta EJB @Stateless to Spring @Component
 *
 * @author ian
 */
@Component("helloBean")
public class HelloBean {

    protected String name;

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     * This method will be intercepted by HelloInterceptor via Spring AOP
     *
     * @param name new value of name
     */
    public void setName(String name) {
        this.name = name;
    }

}
