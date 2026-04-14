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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.io.Serializable;

/**
 * Managed bean for the interceptor example
 *
 * @author ian
 */
@Component("helloBean")
@Scope("request")
public class HelloBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    public HelloBean() {
    }

    @InterceptName
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
