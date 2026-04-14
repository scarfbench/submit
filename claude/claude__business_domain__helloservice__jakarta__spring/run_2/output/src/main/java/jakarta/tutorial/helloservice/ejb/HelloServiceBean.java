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
package jakarta.tutorial.helloservice.ejb;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HelloServiceBean is a REST web service endpoint implemented as a Spring
 * REST controller.
 */

@RestController
@Service
public class HelloServiceBean {
    private final String message = "Hello, ";

    public HelloServiceBean() {}

    @GetMapping("/sayHello")
    public String sayHello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return message + name + ".";
    }
}
