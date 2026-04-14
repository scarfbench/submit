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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

/**
 * @author markito
 */
@Component
public class TokenStore implements Serializable {

    private final List<String> store;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public TokenStore() {
        this.store = new ArrayList<>();
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
