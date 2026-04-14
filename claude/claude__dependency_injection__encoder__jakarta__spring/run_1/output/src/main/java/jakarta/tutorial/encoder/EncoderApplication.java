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
package jakarta.tutorial.encoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot application entry point for the Encoder application.
 * Extends SpringBootServletInitializer to support WAR deployment.
 */
@SpringBootApplication
public class EncoderApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(EncoderApplication.class, args);
    }
}
