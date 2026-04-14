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
package jakarta.tutorial.helloservice.ejb;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * HelloServiceBean is a web service endpoint implemented as an application-scoped bean.
 * Migrated from Jakarta EE Stateless EJB to Quarkus with CXF.
 */

@ApplicationScoped
@WebService(serviceName = "HelloServiceBeanService",
            targetNamespace = "http://ejb.helloservice.tutorial.jakarta/")
public class HelloServiceBean {
    private final String message = "Hello, ";

    public HelloServiceBean() {}

    @WebMethod
    public String sayHello(String name) {
        return message + name + ".";
    }
}
