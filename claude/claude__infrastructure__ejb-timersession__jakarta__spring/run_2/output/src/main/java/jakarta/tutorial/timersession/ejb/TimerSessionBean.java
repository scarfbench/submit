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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * TimerBean is a Spring service that creates a timer and prints out a
 * message when a timeout occurs.
 * Migrated from Jakarta EE EJB Singleton to Spring Service
 */
@Service
public class TimerSessionBean {

    private final TaskScheduler taskScheduler;

    private Date lastProgrammaticTimeout;
    private Date lastAutomaticTimeout;

    private static final Logger logger =
            LoggerFactory.getLogger(TimerSessionBean.class);

    public TimerSessionBean(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void setTimer(long intervalDuration) {
        logger.info("Setting a programmatic timeout for {} milliseconds from now.",
                intervalDuration);
        taskScheduler.schedule(
                this::programmaticTimeout,
                new Date(System.currentTimeMillis() + intervalDuration)
        );
    }

    public void programmaticTimeout() {
        this.setLastProgrammaticTimeout(new Date());
        logger.info("Programmatic timeout occurred.");
    }

    @Scheduled(cron = "0 */1 * * * *")
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
