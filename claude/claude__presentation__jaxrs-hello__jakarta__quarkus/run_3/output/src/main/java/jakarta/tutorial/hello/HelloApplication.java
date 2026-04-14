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
package jakarta.tutorial.hello;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Quarkus auto-configures JAX-RS endpoints without requiring this class.
 * Keeping it for compatibility but it's optional in Quarkus.
 *
 * @author ievans
 */
@ApplicationPath("/")
public class HelloApplication extends Application {

}
