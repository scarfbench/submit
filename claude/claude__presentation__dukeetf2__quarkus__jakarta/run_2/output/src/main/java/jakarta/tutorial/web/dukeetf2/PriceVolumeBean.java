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
package jakarta.tutorial.web.dukeetf2;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/* Updates price and volume information every second */
@ApplicationScoped
public class PriceVolumeBean {
    @Inject
    ETFEndpoint etfEndpoint;

    private Random random;
    private volatile double price = 100.0;
    private volatile int volume = 300000;
    private static final Logger logger = Logger.getLogger("PriceVolumeBean");
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        /* Initialize the bean */
        logger.log(Level.INFO, "Initializing PriceVolumeBean.");
        random = new Random();

        /* Start scheduled task */
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updatePriceAndVolume, 1, 1, TimeUnit.SECONDS);
        logger.log(Level.INFO, "Scheduled task started.");
    }

    @PreDestroy
    public void destroy() {
        /* Shutdown scheduler */
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.log(Level.INFO, "Scheduled task stopped.");
        }
    }

    public void updatePriceAndVolume() {
        /* Adjust price and volume and send updates */
        price += 1.0*(random.nextInt(100)-50)/100.0;
        volume += random.nextInt(5000) - 2500;
        logger.log(Level.INFO, "Scheduled task running - Price: {0}, Volume: {1}", new Object[]{price, volume});
        etfEndpoint.send(price, volume);
    }
}
