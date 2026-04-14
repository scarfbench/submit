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
package jakarta.tutorial.customer.ejb;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.tutorial.customer.data.Customer;

/**
 * Customer Service Bean for client operations
 *
 * @author ievans
 */
@Service
public class CustomerBean {

    protected RestTemplate restTemplate;
    private static final Logger logger = Logger.getLogger(CustomerBean.class.getName());

    @Value("${server.port:8080}")
    private int serverPort;

    private String baseUrl;

    @PostConstruct
    private void init() {
        restTemplate = new RestTemplate();
        baseUrl = "http://localhost:" + serverPort + "/webapi/Customer";
    }

    @PreDestroy
    private void clean() {
        // RestTemplate doesn't require explicit cleanup
    }

    public String createCustomer(Customer customer) {
        if (customer == null) {
            logger.log(Level.WARNING, "customer is null.");
            return "customerError";
        }
        String navigation;
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    baseUrl,
                    customer,
                    Void.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                navigation = "customerCreated";
            } else {
                logger.log(Level.WARNING,
                        "couldn''t create customer with id {0}. Status returned was {1}",
                        new Object[]{customer.getId(), response.getStatusCode()});
                navigation = "customerError";
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating customer", e);
            navigation = "customerError";
        }
        return navigation;
    }

    public String retrieveCustomer(String id) {
        String navigation;
        try {
            Customer customer = restTemplate.getForObject(
                    baseUrl + "/" + id,
                    Customer.class);
            if (customer == null) {
                navigation = "customerError";
            } else {
                navigation = "customerRetrieved";
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving customer", e);
            navigation = "customerError";
        }
        return navigation;
    }

    public List<Customer> retrieveAllCustomers() {
        try {
            ResponseEntity<List<Customer>> response = restTemplate.exchange(
                    baseUrl + "/all",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Customer>>() {
                    });
            return response.getBody();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving all customers", e);
            return null;
        }
    }
}
