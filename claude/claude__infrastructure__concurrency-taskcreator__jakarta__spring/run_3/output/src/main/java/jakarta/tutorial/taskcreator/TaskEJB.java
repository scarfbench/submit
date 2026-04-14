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
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
@RestController
@RequestMapping("/taskinfo")
public class TaskEJB {

    private static final Logger log = Logger.getLogger("TaskEJB");

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private TaskScheduler taskScheduler;

    /* Keep track of periodic tasks so we can kill them later */
    private Map<String, ScheduledFuture<?>> periodicTasks;
    /* Keep the log (textarea content) for all clients in this service */
    private String infoField;
    /* Spring event publisher for the WebSocket endpoint */
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostConstruct
    public void init() {
        periodicTasks = new HashMap<>();
        infoField = "";
    }

    @PreDestroy
    public void destroy() {
        /* Cancel periodic tasks */
        log.info("[TaskEJB] Cancelling periodic tasks");
        for (ScheduledFuture<?> fut : periodicTasks.values())
            fut.cancel(true);
        taskExecutor.shutdown();
    }

    public void submitTask(Task task, String type) {
        /*
         * Use Spring's task executor and scheduler
         * to schedule the tasks
         */
        switch (type) {
            case "IMMEDIATE":
                taskExecutor.submit(task);
                break;
            case "DELAYED":
                taskScheduler.schedule(task,
                    new java.util.Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3)));
                break;
            case "PERIODIC":
                ScheduledFuture<?> fut;
                fut = taskScheduler.scheduleAtFixedRate(task,
                    java.time.Duration.ofSeconds(8));
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

    @PostMapping
    /* The tasks post updates to this REST endpoint */
    public void addToInfoField(@RequestBody String msg) {
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
