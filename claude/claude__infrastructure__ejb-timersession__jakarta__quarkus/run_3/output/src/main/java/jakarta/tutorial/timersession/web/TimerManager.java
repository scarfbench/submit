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
package jakarta.tutorial.timersession.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.tutorial.timersession.ejb.TimerSessionBean;

/**
 * Timer Manager bean for managing timer operations.
 * Migrated from JSF SessionScoped bean to ApplicationScoped for Quarkus.
 *
 * @author ian
 */
@ApplicationScoped
public class TimerManager {

    @Inject
    TimerSessionBean timerSession;

    /**
     * @return the lastTimeout
     */
    public String getLastProgrammaticTimeout() {
        return timerSession.getLastProgrammaticTimeout();
    }

    public void setTimer() {
        long timeoutDuration = 8000;
        timerSession.setTimer(timeoutDuration);
    }

    /**
     * @return the lastAutomaticTimeout
     */
    public String getLastAutomaticTimeout() {
        return timerSession.getLastAutomaticTimeout();
    }

}
