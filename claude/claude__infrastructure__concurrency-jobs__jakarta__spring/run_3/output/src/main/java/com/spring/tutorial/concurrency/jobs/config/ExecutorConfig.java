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
package com.spring.tutorial.concurrency.jobs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for thread pool executors
 * Replaces Jakarta ManagedExecutorService with Spring ThreadPoolTaskExecutor
 */
@Configuration
public class ExecutorConfig {

    @Value("${executor.high-priority.core-pool-size:10}")
    private int highPrioCorePoolSize;

    @Value("${executor.high-priority.max-pool-size:20}")
    private int highPrioMaxPoolSize;

    @Value("${executor.high-priority.queue-capacity:100}")
    private int highPrioQueueCapacity;

    @Value("${executor.low-priority.core-pool-size:1}")
    private int lowPrioCorePoolSize;

    @Value("${executor.low-priority.max-pool-size:5}")
    private int lowPrioMaxPoolSize;

    @Value("${executor.low-priority.queue-capacity:50}")
    private int lowPrioQueueCapacity;

    @Bean(name = "highPriorityExecutor")
    public Executor highPriorityExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(highPrioCorePoolSize);
        executor.setMaxPoolSize(highPrioMaxPoolSize);
        executor.setQueueCapacity(highPrioQueueCapacity);
        executor.setThreadNamePrefix("high-prio-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "lowPriorityExecutor")
    public Executor lowPriorityExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(lowPrioCorePoolSize);
        executor.setMaxPoolSize(lowPrioMaxPoolSize);
        executor.setQueueCapacity(lowPrioQueueCapacity);
        executor.setThreadNamePrefix("low-prio-");
        executor.initialize();
        return executor;
    }
}
