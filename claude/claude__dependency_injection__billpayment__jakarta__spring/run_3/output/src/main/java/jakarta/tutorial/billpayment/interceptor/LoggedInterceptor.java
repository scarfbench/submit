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
package jakarta.tutorial.billpayment.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Spring AOP aspect for logging method invocations.
 * Intercepts methods annotated with @Logged.
 */
@Aspect
@Component
public class LoggedInterceptor {

    public LoggedInterceptor() {
    }

    @Around("@annotation(jakarta.tutorial.billpayment.interceptor.Logged) || @within(jakarta.tutorial.billpayment.interceptor.Logged)")
    public Object logMethodEntry(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        System.out.println("Entering method: "
                + signature.getMethod().getName() + " in class "
                + signature.getMethod().getDeclaringClass().getName());

        return joinPoint.proceed();
    }
}
