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

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Web application lifecycle listener.
 */
@WebListener
public class SimpleServletListener implements ServletContextListener {

    static final Logger log =
            Logger.getLogger("mood.web.SimpleServletListener");

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Context initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Context destroyed");
    }

    void attributeAdded(String name, Object value) {
        log.log(Level.INFO, "Attribute {0} has been added, with value: {1}",
                new Object[]{name, value});
    }

    void attributeRemoved(String name) {
        log.log(Level.INFO, "Attribute {0} has been removed", name);
    }

    void attributeReplaced(String name, Object value) {
        log.log(Level.INFO, "Attribute {0} has been replaced, with value: {1}",
                new Object[]{name, value});
    }

}
