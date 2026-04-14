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
package quarkus.tutorial.interceptor;

import java.util.logging.Logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Spring AOP Aspect for intercepting method calls
 *
 * @author ian
 */
@Aspect
@Component
public class HelloInterceptor {
    protected String greeting;
    private static final Logger logger = Logger.getLogger("interceptor.ejb.HelloInterceptor");

    public HelloInterceptor() {
    }

    @Around("@annotation(InterceptName)")
    public Object modifyGreeting(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] parameters = joinPoint.getArgs();
        if (parameters.length > 0 && parameters[0] instanceof String) {
            String param = (String) parameters[0];
            param = param.toLowerCase();
            parameters[0] = param;
        }
        try {
            return joinPoint.proceed(parameters);
        } catch (Exception e) {
            logger.warning("Error calling proceed in modifyGreeting()");
            return null;
        }
    }

}
