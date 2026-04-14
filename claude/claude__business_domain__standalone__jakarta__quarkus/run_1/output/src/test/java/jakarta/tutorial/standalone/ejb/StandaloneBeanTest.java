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

import jakarta.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

/**
 *
 * @author ian
 */
@QuarkusTest
public class StandaloneBeanTest {

    private static final Logger logger = Logger.getLogger("standalone.ejb");

    @Inject
    StandaloneBean standaloneBean;

    public StandaloneBeanTest() {
    }

    /**
     * Test of returnMessage method, of class StandaloneBean.
     * @throws java.lang.Exception
     */
    @Test
    public void testReturnMessage() throws Exception {
        logger.info("Testing standalone.ejb.StandaloneBean.returnMessage()");
        String expResult = "Greetings!";
        String result = standaloneBean.returnMessage();
        assertEquals(expResult, result);
    }
}
