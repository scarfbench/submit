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

import jakarta.tutorial.helloservice.ejb.HelloServiceBean;
import jakarta.xml.ws.Endpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServiceConfig {

    private final HelloServiceBean helloServiceBean;

    public WebServiceConfig(HelloServiceBean helloServiceBean) {
        this.helloServiceBean = helloServiceBean;
    }

    @Bean
    public Endpoint helloServiceEndpoint() {
        Endpoint endpoint = Endpoint.create(helloServiceBean);
        endpoint.publish("/HelloServiceBeanService/HelloServiceBean");
        return endpoint;
    }
}
