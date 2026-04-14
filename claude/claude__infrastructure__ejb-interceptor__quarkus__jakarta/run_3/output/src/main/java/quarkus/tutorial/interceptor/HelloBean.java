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
package quarkus.tutorial.interceptor;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.interceptor.Interceptors;

/**
 * Managed Bean for the Interceptor example
 *
 * @author ian
 */
@Named
@RequestScoped
@Interceptors(HelloInterceptor.class)
public class HelloBean {
    private String name;

    public HelloBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
