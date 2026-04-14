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
package com.example.spring.producermethods;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Configuration class that produces Coder beans based on the coderType
 */
@Configuration
public class CoderConfiguration {

    private final static int TEST = 1;
    private final static int SHIFT = 2;

    /**
     * Producer method that chooses between two beans based on the coderType
     * value from CoderBean.
     *
     * @param coderBean the CoderBean containing coderType
     * @return Chosen coder implementation
     */
    @Bean
    @Chosen
    @RequestScope
    public Coder getCoder(CoderBean coderBean) {
        switch (coderBean.getCoderType()) {
            case TEST:
                return new TestCoderImpl();
            case SHIFT:
                return new CoderImpl();
            default:
                return new CoderImpl();
        }
    }
}
