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
package jakarta.tutorial.helloservice.service;

import org.springframework.stereotype.Service;

/**
 * HelloService is a service component that provides greeting functionality.
 */
@Service
public class HelloService {
    private final String message = "Hello, ";

    public HelloService() {}

    public String sayHello(String name) {
        return message + name + ".";
    }
}
