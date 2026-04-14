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
package spring.tutorial.interceptor;

import org.springframework.stereotype.Service;

/**
 * Service bean for the interceptor example
 */
@Service
public class HelloBean {

    private String name;

    public HelloBean() {
    }

    @Intercepted
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
