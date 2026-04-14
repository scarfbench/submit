/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * CDI Producer for Jackson ObjectMapper
 */
@ApplicationScoped
public class JacksonProducer {

    @Produces
    @ApplicationScoped
    public ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}
