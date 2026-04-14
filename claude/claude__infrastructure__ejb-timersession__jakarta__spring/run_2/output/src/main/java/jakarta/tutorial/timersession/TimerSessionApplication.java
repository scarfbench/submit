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
package jakarta.tutorial.timersession;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Application Entry Point
 * Migrated from Jakarta EE EJB Timer Session Example
 */
@SpringBootApplication
@EnableScheduling
public class TimerSessionApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimerSessionApplication.class, args);
    }
}
