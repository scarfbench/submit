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
package spring.tutorial.customer.client;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import spring.tutorial.customer.data.Customer;

/**
 * Customer REST Client using Spring WebClient
 *
 * @author ievans
 */
@Component
public class CustomerClient {

    private static final Logger logger = Logger.getLogger(CustomerClient.class.getName());
    private final WebClient webClient;

    public CustomerClient(@Value("${server.port:8080}") int serverPort) {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:" + serverPort + "/webapi/Customer")
                .build();
    }

    public boolean createCustomer(Customer customer) {
        if (customer == null) {
            logger.log(Level.WARNING, "customer is null.");
            return false;
        }

        try {
            webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(customer)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException e) {
            logger.log(Level.WARNING,
                    "couldn''t create customer with id {0}. Status returned was {1}",
                    new Object[]{customer.getId(), e.getStatusCode()});
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating customer", e);
            return false;
        }
    }

    public Customer retrieveCustomer(String id) {
        try {
            return webClient.get()
                    .uri("/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Customer.class)
                    .block();
        } catch (WebClientResponseException e) {
            logger.log(Level.WARNING, "Customer not found with id: {0}", id);
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving customer", e);
            return null;
        }
    }

    public List<Customer> retrieveAllCustomers() {
        try {
            return webClient.get()
                    .uri("/all")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Customer>>() {})
                    .block();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error retrieving all customers: {0}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
