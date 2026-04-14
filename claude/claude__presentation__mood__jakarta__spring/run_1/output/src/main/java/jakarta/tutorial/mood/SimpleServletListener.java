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
package jakarta.tutorial.mood;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletContextAttributeEvent;
import jakarta.servlet.ServletContextAttributeListener;

/**
 * Web application lifecycle listener.
 */
@Component
public class SimpleServletListener implements ServletContextAttributeListener {

    static final Logger log =
            Logger.getLogger("mood.web.SimpleServletListener");

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.info("Context initialized");
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        log.info("Context destroyed");
    }

    @Override
    public void attributeAdded(ServletContextAttributeEvent event) {
        log.log(Level.INFO, "Attribute {0} has been added, with value: {1}",
                new Object[]{event.getName(), event.getValue()});
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent event) {
        log.log(Level.INFO, "Attribute {0} has been removed",
                event.getName());
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent event) {
        log.log(Level.INFO, "Attribute {0} has been replaced, with value: {1}",
                new Object[]{event.getName(), event.getValue()});
    }
}
