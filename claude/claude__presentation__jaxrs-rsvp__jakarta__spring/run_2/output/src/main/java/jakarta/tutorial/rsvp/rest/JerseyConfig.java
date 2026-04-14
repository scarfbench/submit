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
package jakarta.tutorial.rsvp.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;
import jakarta.tutorial.rsvp.ejb.ResponseBean;
import jakarta.tutorial.rsvp.ejb.StatusBean;

/**
 * Jersey configuration for JAX-RS resources
 */
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(ResponseBean.class);
        register(StatusBean.class);
    }
}
