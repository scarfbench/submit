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
package spring.tutorial.web.dukeetf2;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/* Updates price and volume information every second */
@Component
public class PriceVolumeBean {
    @Autowired
    private ETFEndpoint etfEndpoint;

    private Random random;
    private volatile double price = 100.0;
    private volatile int volume = 300000;
    private static final Logger logger = Logger.getLogger("PriceVolumeBean");

    @PostConstruct
    public void init() {
        /* Initialize the bean */
        logger.log(Level.INFO, "Initializing PriceVolumeBean.");
        random = new Random();
    }

    @Scheduled(fixedRate = 1000)
    public void updatePriceAndVolume() {
        /* Adjust price and volume and send updates */
        price += 1.0*(random.nextInt(100)-50)/100.0;
        volume += random.nextInt(5000) - 2500;
        logger.log(Level.INFO, "Scheduled task running - Price: {0}, Volume: {1}", new Object[]{price, volume});
        etfEndpoint.send(price, volume);
    }
}
