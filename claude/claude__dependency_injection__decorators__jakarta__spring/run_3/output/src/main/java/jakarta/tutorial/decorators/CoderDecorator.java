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
package jakarta.tutorial.decorators;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Spring AOP aspect that decorates Coder interface implementations
 * (migrated from CDI @Decorator)
 */
@Aspect
@Component
public class CoderDecorator {

    /**
     * Around advice that wraps the codeString method to add formatting
     */
    @Around("execution(* jakarta.tutorial.decorators.CoderImpl.codeString(..)) && args(s, tval)")
    public Object decorateCodeString(ProceedingJoinPoint joinPoint, String s, int tval) throws Throwable {
        int len = s.length();

        // Call the actual method
        String result = (String) joinPoint.proceed();

        // Decorate the result
        return "\"" + s + "\" becomes " + "\"" + result
                + "\", " + len + " characters in length";
    }
}
