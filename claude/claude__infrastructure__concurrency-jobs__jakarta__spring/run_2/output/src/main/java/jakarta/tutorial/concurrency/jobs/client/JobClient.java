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

import java.io.Serializable;
import java.util.logging.Logger;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import org.springframework.stereotype.Component;

/**
 * Client to REST service - migrated from JSF managed bean to Spring component
 * Note: JSF UI has been removed in favor of REST-only API
 * This client can be used programmatically or for testing purposes
 *
 * @author markito
 */
@Component
public class JobClient implements Serializable {
    private final static Logger logger = Logger.getLogger(JobClient.class.getCanonicalName());
    private static final long serialVersionUID = 16472027766900196L;

    private String token;
    private int jobID;

    private final String serviceEndpoint = "http://localhost:8080/webapi/JobService/process";

    /**
     * Submit a job to the REST service
     * @return HTTP status code as string
     */
    public String submit() {
        final Client client = ClientBuilder.newClient();

        final Response response = client.target(serviceEndpoint)
                .queryParam("jobID", getJobID())
                .request()
                .header("X-REST-API-Key", token)
                .post(null);

        String message;
        if (response.getStatus() == 200) {
            message = String.format("Job %d successfully submitted", getJobID());
            logger.info(message);
        } else {
            message = String.format("Job %d was NOT submitted. Status: %d", getJobID(), response.getStatus());
            logger.warning(message);
        }

        clear();
        return String.valueOf(response.getStatus());
    }

    private void clear() {
        this.token = "";
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the jobID
     */
    public int getJobID() {
        return jobID;
    }

    /**
     * @param jobID the jobID to set
     */
    public void setJobID(int jobID) {
        this.jobID = jobID;
    }
}
