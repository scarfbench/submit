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

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;

/**
 * CDI Producer for EntityManager
 */
@ApplicationScoped
public class EntityManagerProducer {

    private EntityManagerFactory emf;

    public EntityManagerProducer() {
        this.emf = Persistence.createEntityManagerFactory("customerPU");
    }

    @Produces
    @ApplicationScoped
    @PersistenceContext(unitName = "customerPU")
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    @PreDestroy
    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
