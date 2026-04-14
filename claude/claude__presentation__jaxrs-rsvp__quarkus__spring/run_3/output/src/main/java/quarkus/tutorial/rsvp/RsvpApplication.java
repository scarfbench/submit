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
package quarkus.tutorial.rsvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletContext;

@SpringBootApplication
public class RsvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(RsvpApplication.class, args);
    }

    @Bean
    public ServletRegistrationBean<FacesServlet> facesServletRegistration() {
        ServletRegistrationBean<FacesServlet> registration = new ServletRegistrationBean<>(new FacesServlet(), "*.xhtml");
        registration.setName("Faces Servlet");
        registration.setLoadOnStartup(1);
        return registration;
    }

    @Bean
    public jakarta.servlet.ServletContextListener contextListener() {
        return new jakarta.servlet.ServletContextListener() {
            @Override
            public void contextInitialized(jakarta.servlet.ServletContextEvent sce) {
                ServletContext servletContext = sce.getServletContext();
                servletContext.setInitParameter("jakarta.faces.DEFAULT_SUFFIX", ".xhtml");
                servletContext.setInitParameter("jakarta.faces.FACELETS_REFRESH_PERIOD", "-1");
                servletContext.setInitParameter("jakarta.faces.STATE_SAVING_METHOD", "server");
                servletContext.setInitParameter("jakarta.faces.PROJECT_STAGE", "Development");
                servletContext.setInitParameter("jakarta.faces.FACELETS_SKIP_COMMENTS", "true");
            }
        };
    }
}
