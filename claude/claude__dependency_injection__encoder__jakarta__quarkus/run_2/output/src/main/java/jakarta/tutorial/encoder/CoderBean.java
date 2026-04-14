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
package jakarta.tutorial.encoder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Service that calls a Coder implementation to perform a transformation
 * on an input string
 */
@ApplicationScoped
public class CoderBean {

    @Inject
    Coder coder;

    public String encodeString(String inputString, int transVal) {
        return coder.codeString(inputString, transVal);
    }
}
