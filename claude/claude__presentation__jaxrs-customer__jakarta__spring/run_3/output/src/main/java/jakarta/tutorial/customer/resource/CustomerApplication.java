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
package jakarta.tutorial.customer.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot Application Entry Point
 *
 * @author ievans
 */
@SpringBootApplication(scanBasePackages = "jakarta.tutorial.customer")
@EntityScan(basePackages = "jakarta.tutorial.customer.data")
public class CustomerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }
}
