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
package jakarta.tutorial.helloservice.ws;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.tutorial.helloservice.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * HelloWebService is a SOAP web service endpoint.
 */
@Component
@WebService(serviceName = "HelloService")
public class HelloWebService {

    private final HelloService helloService;

    @Autowired
    public HelloWebService(HelloService helloService) {
        this.helloService = helloService;
    }

    @WebMethod
    public String sayHello(String name) {
        return helloService.sayHello(name);
    }
}
