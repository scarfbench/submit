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

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ServletConfig {

    @Bean
    public ServletRegistrationBean<MoodServlet> moodServlet() {
        ServletRegistrationBean<MoodServlet> registrationBean =
            new ServletRegistrationBean<>(new MoodServlet(), "/report");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<TimeOfDayFilter> timeOfDayFilter() {
        FilterRegistrationBean<TimeOfDayFilter> registrationBean =
            new FilterRegistrationBean<>();
        registrationBean.setFilter(new TimeOfDayFilter());
        registrationBean.addUrlPatterns("/*");

        Map<String, String> initParams = new HashMap<>();
        initParams.put("mood", "awake");
        registrationBean.setInitParameters(initParams);

        return registrationBean;
    }

    @Bean
    public ServletListenerRegistrationBean<SimpleServletListener> servletListener() {
        return new ServletListenerRegistrationBean<>(new SimpleServletListener());
    }
}
