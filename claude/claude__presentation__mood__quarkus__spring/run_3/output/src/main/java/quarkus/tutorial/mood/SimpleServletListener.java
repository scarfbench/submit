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
package quarkus.tutorial.mood;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Web application lifecycle listener.
 */
@Component
public class SimpleServletListener {

    static final Logger log =
            Logger.getLogger("mood.web.SimpleServletListener");


    @EventListener
    public void onStart(ApplicationReadyEvent event) {
        log.info("Context initialized");
    }

    @EventListener
    public void onStop(ContextClosedEvent event) {
        log.info("Context destroyed");
    }

    public void attributeAdded(String name, Object value) {
        log.log(Level.INFO, "Attribute {0} has been added, with value: {1}",
                new Object[]{name, value});
    }

    public void attributeRemoved(String name) {
        log.log(Level.INFO, "Attribute {0} has been removed", name);
    }

    public void attributeReplaced(String name, Object value) {
        log.log(Level.INFO, "Attribute {0} has been replaced, with value: {1}",
                new Object[]{name, value});
    }

}
