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
package quarkus.tutorial.customer.util;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceContext;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transaction management interceptor for manual transaction handling
 */
@Transactional
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class TransactionInterceptor {

    private static final Logger logger = Logger.getLogger(TransactionInterceptor.class.getName());

    @AroundInvoke
    public Object manageTransaction(InvocationContext context) throws Exception {
        Object result;

        // Get EntityManager from target object
        Object target = context.getTarget();
        EntityManager em = null;

        try {
            // Use reflection to get EntityManager field
            em = (EntityManager) target.getClass().getDeclaredField("em").get(target);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not get EntityManager from target", e);
            return context.proceed();
        }

        EntityTransaction transaction = null;
        boolean owner = false;

        try {
            if (em != null) {
                transaction = em.getTransaction();
                if (!transaction.isActive()) {
                    transaction.begin();
                    owner = true;
                }
            }

            result = context.proceed();

            if (owner && transaction != null && transaction.isActive()) {
                transaction.commit();
            }
        } catch (Exception e) {
            if (owner && transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }

        return result;
    }
}
