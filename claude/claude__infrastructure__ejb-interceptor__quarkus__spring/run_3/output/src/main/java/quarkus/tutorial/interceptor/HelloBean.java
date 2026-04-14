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

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;

/**
 * JSF Managed Bean for the interceptor example.
 * This bean is used by the JSF views to capture and display user input.
 */
@Component("helloBean")
@Scope("view")
public class HelloBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;

    public HelloBean() {
    }

    /**
     * Gets the name entered by the user.
     * This method will be intercepted by HelloInterceptor.
     * @return the user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name entered by the user.
     * @param name the user's name
     */
    public void setName(String name) {
        this.name = name;
    }
}
