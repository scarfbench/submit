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
package jakarta.tutorial.concurrency.jobs.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * REST client for JobService using Spring RestTemplate
 */
@Component
public class JobServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public JobServiceClient(@Value("${job-service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public ResponseEntity<String> processJob(int jobID, String apiKey) {
        String url = baseUrl + "/webapi/JobService/process?jobID=" + jobID;

        HttpHeaders headers = new HttpHeaders();
        if (apiKey != null) {
            headers.set("X-REST-API-Key", apiKey);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}
