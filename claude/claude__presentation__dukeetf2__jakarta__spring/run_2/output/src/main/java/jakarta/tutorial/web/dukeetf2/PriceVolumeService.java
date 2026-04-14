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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Random;

/* Updates price and volume information every second */
@Service
public class PriceVolumeService {
    private static final Logger logger = LoggerFactory.getLogger(PriceVolumeService.class);

    private Random random;
    private volatile double price = 100.0;
    private volatile int volume = 300000;

    @PostConstruct
    public void init() {
        /* Initialize the service */
        logger.info("Initializing PriceVolumeService.");
        random = new Random();
    }

    @Scheduled(fixedRate = 1000)
    public void updatePriceAndVolume() {
        /* Adjust price and volume and send updates */
        price += 1.0 * (random.nextInt(100) - 50) / 100.0;
        volume += random.nextInt(5000) - 2500;
        ETFWebSocketHandler.send(price, volume);
    }
}
