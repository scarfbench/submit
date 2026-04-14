/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 * SPDX-License-License: BSD-3-Clause
 */
package quarkus.tutorial.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
