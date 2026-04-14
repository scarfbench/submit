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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Service
@Path("/taskinfo")
public class TaskService {

    private static final Logger log = Logger.getLogger("TaskService");

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private ThreadPoolTaskExecutor taskExecutor;
    private ThreadPoolTaskScheduler taskScheduler;

    /* Keep track of periodic tasks so we can kill them later */
    private Map<String, ScheduledFuture<?>> periodicTasks;
    /* Keep the log (textarea content) for all clients in this service */
    private String infoField;

    @PostConstruct
    public void init() {
        periodicTasks = new HashMap<>();
        infoField = "";

        // Initialize task executor for immediate tasks
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setQueueCapacity(25);
        taskExecutor.setThreadNamePrefix("task-executor-");
        taskExecutor.initialize();

        // Initialize task scheduler for delayed/periodic tasks
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        taskScheduler.setThreadNamePrefix("task-scheduler-");
        taskScheduler.initialize();
    }

    @PreDestroy
    public void destroy() {
        /* Cancel periodic tasks */
        log.info("[TaskService] Cancelling periodic tasks");
        for (ScheduledFuture<?> fut : periodicTasks.values())
            fut.cancel(true);
        if (taskExecutor != null) {
            taskExecutor.shutdown();
        }
        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
    }

    public void submitTask(Task task, String type) {
        /*
         * Use the Spring task executor and scheduler
         * to schedule the tasks
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
            log.log(Level.INFO, "[TaskService] Cancelling task {0}", name);
            periodicTasks.get(name).cancel(true);
            periodicTasks.remove(name);
            /* Notify the WebSocket endpoint to update the client's task list */
            eventPublisher.publishEvent(new TaskEvent(this, "tasklist"));
        }
    }

    @POST
    @Consumes({ "text/html", "text/plain" })
    /* The tasks post updates to this JAX-RS endpoint */
    public void addToInfoField(String msg) {
        /* Update the log */
        infoField = msg + "\n" + infoField;
        log.log(Level.INFO, "[TaskService] Added message {0}", msg);
        /* Notify the WebSocket endpoint to update the client's task log */
        eventPublisher.publishEvent(new TaskEvent(this, "infobox"));
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
