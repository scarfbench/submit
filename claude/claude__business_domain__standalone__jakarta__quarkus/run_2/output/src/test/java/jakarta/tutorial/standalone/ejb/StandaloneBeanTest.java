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
package jakarta.tutorial.standalone.ejb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 *
 * @author ian
 */
@QuarkusTest
public class StandaloneBeanTest {

    private static final Logger logger = Logger.getLogger("standalone.ejb");

    @Inject
    StandaloneBean standaloneBean;

    /**
     * Test of returnMessage method, of class StandaloneBean.
     */
    @Test
    public void testReturnMessage() {
        logger.info("Testing standalone.ejb.StandaloneBean.returnMessage()");
        String expResult = "Greetings!";
        String result = standaloneBean.returnMessage();
        assertEquals(expResult, result);
    }
}
