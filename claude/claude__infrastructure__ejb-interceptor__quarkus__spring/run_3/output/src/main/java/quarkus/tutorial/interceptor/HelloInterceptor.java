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
 * Spring AOP Aspect for intercepting method calls.
 * This aspect intercepts calls to the getName() method and converts
 * the returned string to lowercase.
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

    /**
     * Intercepts calls to getName() method in HelloBean and modifies the result.
     * Converts the string parameter/result to lowercase.
     *
     * @param joinPoint the join point
     * @return the modified result
     * @throws Throwable if an error occurs
     */
    @Around("execution(* quarkus.tutorial.interceptor.HelloBean.getName(..))")
    public Object modifyGreeting(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // Proceed with the original method call
            Object result = joinPoint.proceed();

            // If result is a String, convert to lowercase
            if (result instanceof String) {
                String modifiedResult = ((String) result).toLowerCase();
                logger.info("Modified greeting from '" + result + "' to '" + modifiedResult + "'");
                return modifiedResult;
            }

            return result;
        } catch (Exception e) {
            logger.warning("Error in modifyGreeting() interceptor: " + e.getMessage());
            return null;
        }
    }

}
