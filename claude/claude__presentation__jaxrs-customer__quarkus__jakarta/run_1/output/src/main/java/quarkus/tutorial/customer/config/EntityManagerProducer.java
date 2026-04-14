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
package quarkus.tutorial.customer.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * CDI Producer for EntityManager
 */
@ApplicationScoped
public class EntityManagerProducer {

    private EntityManagerFactory emf;

    @Produces
    @ApplicationScoped
    public EntityManagerFactory createEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("CustomerPU");
        }
        return emf;
    }

    @Produces
    public EntityManager createEntityManager(EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    public void closeEntityManager(@Disposes EntityManager em) {
        if (em.isOpen()) {
            em.close();
        }
    }

    public void closeEntityManagerFactory(@Disposes EntityManagerFactory emf) {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}
