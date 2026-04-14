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
package com.example.counter;

import org.springframework.stereotype.Service;

/**
 *
 * @author ian
 * CounterService is a simple singleton service that records the number
 * of hits to a web page.
 * Migrated from Jakarta EJB @Singleton to Spring @Service
 */
@Service
public class CounterService {
    private int hits = 1;

    // Increment and return the number of hits
    public synchronized int getHits() {
        return hits++;
    }
}
