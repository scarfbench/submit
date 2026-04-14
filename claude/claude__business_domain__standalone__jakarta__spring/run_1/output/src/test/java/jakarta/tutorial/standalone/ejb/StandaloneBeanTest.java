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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author ian
 */
@SpringBootTest
public class StandaloneBeanTest {

    @Autowired
    private StandaloneBean standaloneBean;

    private static final Logger logger = Logger.getLogger("standalone.ejb");

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
