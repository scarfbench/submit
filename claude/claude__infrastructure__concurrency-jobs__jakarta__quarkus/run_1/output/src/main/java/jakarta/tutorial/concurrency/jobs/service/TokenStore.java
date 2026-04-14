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
package jakarta.tutorial.concurrency.jobs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author markito
 * Migrated to Quarkus: @Singleton replaced with @ApplicationScoped
 * Manual locking replaces EJB @Lock annotations
 */
@ApplicationScoped
public class TokenStore {

    private final List<String> store;
    private final ReadWriteLock lock;

    public TokenStore() {
        this.store = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
    }

    public void put(String key) {
        lock.writeLock().lock();
        try {
            store.add(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isValid(String key) {
        lock.readLock().lock();
        try {
            return store.contains(key);
        } finally {
            lock.readLock().unlock();
        }
    }
}
