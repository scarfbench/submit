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
package jakarta.tutorial.helloservice.config;

import jakarta.tutorial.helloservice.ws.HelloWebService;
import jakarta.xml.ws.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for SOAP web services.
 */
@Configuration
public class WebServiceConfig {

    @Autowired
    private HelloWebService helloWebService;

    @Bean
    public Endpoint endpoint() {
        Endpoint endpoint = Endpoint.create(helloWebService);
        endpoint.publish("/ws/hello");
        return endpoint;
    }
}
