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
package jakarta.tutorial.taskcreator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
@Path("/taskinfo")
public class TaskEJB {

    private static final Logger log = Logger.getLogger("TaskEJB");

    /* Use Spring's task executors instead of Jakarta EE managed executors */
    private final ThreadPoolTaskExecutor taskExecutor;
    private final ThreadPoolTaskScheduler taskScheduler;

    /* Keep track of periodic tasks so we can kill them later */
    private Map<String, ScheduledFuture<?>> periodicTasks;
    /* Keep the log (textarea content) for all clients in this component */
    private String infoField;

    /* Use Spring's event publisher for CDI events */
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public TaskEJB() {
        // Initialize Spring task executors
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(5);
        this.taskExecutor.setMaxPoolSize(10);
        this.taskExecutor.setQueueCapacity(25);
        this.taskExecutor.setThreadNamePrefix("task-executor-");
        this.taskExecutor.initialize();

        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.taskScheduler.setPoolSize(5);
        this.taskScheduler.setThreadNamePrefix("task-scheduler-");
        this.taskScheduler.initialize();
    }

    @PostConstruct
    public void init() {
        periodicTasks = new HashMap<>();
        infoField = "";
        log.info("[TaskEJB] Initialized with Spring executors");
    }

    @PreDestroy
    public void destroy() {
        /* Cancel periodic tasks */
        log.info("[TaskEJB] Cancelling periodic tasks");
        for (ScheduledFuture<?> fut : periodicTasks.values())
            fut.cancel(true);
        taskExecutor.shutdown();
        taskScheduler.shutdown();
    }

    public void submitTask(Task task, String type) {
        /*
         * Use Spring's task executors to schedule the tasks
         */
        switch (type) {
            case "IMMEDIATE":
                taskExecutor.submit(task);
                break;
            case "DELAYED":
                taskScheduler.schedule(task,
                    new java.util.Date(System.currentTimeMillis() + 3000));
                break;
            case "PERIODIC":
                ScheduledFuture<?> fut;
                fut = taskScheduler.scheduleAtFixedRate(task, 8000);
                periodicTasks.put(task.getName(), fut);
                break;
        }
    }

    public void cancelPeriodicTask(String name) {
        /* Cancel a periodic task */
        if (periodicTasks.containsKey(name)) {
            log.log(Level.INFO, "[TaskEJB] Cancelling task {0}", name);
            periodicTasks.get(name).cancel(true);
            periodicTasks.remove(name);
            /* Notify the WebSocket endpoint to update the client's task list */
            eventPublisher.publishEvent("tasklist");
        }
    }

    @POST
    @Consumes({ "text/html", "text/plain" })
    /* The tasks post updates to this JAX-RS endpoint */
    public void addToInfoField(String msg) {
        /* Update the log */
        infoField = msg + "\n" + infoField;
        log.log(Level.INFO, "[TaskEJB] Added message {0}", msg);
        /* Notify the WebSocket endpoint to update the client's task log */
        eventPublisher.publishEvent("infobox");
    }

    /* Provide the execution log for the client's pages */
    public String getInfoField() {
        return infoField;
    }

    public void clearInfoField() {
        infoField = "";
    }

    /* Provide the list of running tasks */
    public Set<String> getPeriodicTasks() {
        return periodicTasks.keySet();
    }
}
