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
package jakarta.tutorial.timersession.ejb;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

/**
 * TimerSessionBean is a Spring service that creates timers and prints out messages
 * when timeouts occur. Migrated from Jakarta EE EJB Singleton to Spring Service.
 */
@Service
public class TimerSessionBean {

    private final ThreadPoolTaskScheduler taskScheduler;
    private Date lastProgrammaticTimeout;
    private Date lastAutomaticTimeout;

    private static final Logger logger =
            Logger.getLogger("timersession.ejb.TimerSessionBean");

    public TimerSessionBean() {
        // Initialize the task scheduler for programmatic timers
        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.taskScheduler.setPoolSize(5);
        this.taskScheduler.setThreadNamePrefix("timer-");
        this.taskScheduler.initialize();
    }

    public void setTimer(long intervalDuration) {
        logger.log(Level.INFO,
                "Setting a programmatic timeout for {0} milliseconds from now.",
                intervalDuration);

        // Schedule a one-time task after the specified delay
        taskScheduler.schedule(
            () -> programmaticTimeout(),
            new Date(System.currentTimeMillis() + intervalDuration)
        );
    }

    public void programmaticTimeout() {
        this.setLastProgrammaticTimeout(new Date());
        logger.info("Programmatic timeout occurred.");
    }

    // Spring's @Scheduled annotation replaces EJB's @Schedule
    // Runs every minute (cron: at second 0 of every minute)
    @Scheduled(cron = "0 * * * * ?")
    public void automaticTimeout() {
        this.setLastAutomaticTimeout(new Date());
        logger.info("Automatic timeout occurred");
    }

    /**
     * @return the lastTimeout
     */
    public String getLastProgrammaticTimeout() {
        if (lastProgrammaticTimeout != null) {
            return lastProgrammaticTimeout.toString();
        } else {
            return "never";
        }
    }

    /**
     * @param lastTimeout the lastTimeout to set
     */
    public void setLastProgrammaticTimeout(Date lastTimeout) {
        this.lastProgrammaticTimeout = lastTimeout;
    }

    /**
     * @return the lastAutomaticTimeout
     */
    public String getLastAutomaticTimeout() {
        if (lastAutomaticTimeout != null) {
            return lastAutomaticTimeout.toString();
        } else {
            return "never";
        }
    }

    /**
     * @param lastAutomaticTimeout the lastAutomaticTimeout to set
     */
    public void setLastAutomaticTimeout(Date lastAutomaticTimeout) {
        this.lastAutomaticTimeout = lastAutomaticTimeout;
    }
}
