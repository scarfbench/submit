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
package jakarta.tutorial.taskcreator;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.ApplicationPath;

/* This class configures JAX-RS for this application with Spring Boot Jersey.
 * The JAX-RS paths for this application's endpoints are under /api/ */
@Component
@ApplicationPath("/api")
public class JAXRSApplication extends ResourceConfig {

    public JAXRSApplication() {
        register(TaskService.class);
    }
}
