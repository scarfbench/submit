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

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/* This class configures JAX-RS for Quarkus.
 * In Quarkus, JAX-RS resources are automatically discovered,
 * but we keep this for compatibility and explicit path configuration */
@ApplicationPath("/")
public class JAXRSApplication extends Application {
    // Quarkus automatically discovers and registers JAX-RS resources
    // No need to manually register resources
}
