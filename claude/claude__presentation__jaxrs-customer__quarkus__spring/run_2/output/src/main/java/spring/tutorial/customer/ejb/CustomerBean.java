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
package spring.tutorial.customer.ejb;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import spring.tutorial.customer.data.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author ievans
 */

@Component("customerBean")
public class CustomerBean {

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger logger
            = Logger.getLogger(CustomerBean.class.getName());

    private static final String BASE_URL = "http://localhost:8080/webapi/Customer";

    @PostConstruct
    private void init() {
        logger.info("CustomerBean initialized");
    }

    public String createCustomer(Customer customer) {
        if (customer == null) {
            logger.log(Level.WARNING, "customer is null.");
            return "customerError";
        }
        String navigation;
        try {
            HttpEntity<Customer> request = new HttpEntity<>(customer);
            ResponseEntity<Void> response = restTemplate.postForEntity(BASE_URL, request, Void.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                navigation = "list?faces-redirect=true";
            } else {
                logger.log(Level.WARNING,
                        "couldn''t create customer with id {0}. Status returned was {1}",
                        new Object[]{customer.getId(), response.getStatusCode()});
                FacesContext context = FacesContext.getCurrentInstance();
                if (context != null) {
                    context.addMessage(null,
                            new FacesMessage("Could not create customer."));
                }
                navigation = "error";
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating customer", e);
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                context.addMessage(null,
                        new FacesMessage("Could not create customer: " + e.getMessage()));
            }
            navigation = "error";
        }
        return navigation;
    }

    public String retrieveCustomer(String id) {
        String navigation;
        try {
            ResponseEntity<Customer> response = restTemplate.getForEntity(BASE_URL + "/" + id, Customer.class);
            Customer customer = response.getBody();
            if (customer == null) {
                navigation = "customerError";
            } else {
                navigation = "customerRetrieved";
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error retrieving customer with id " + id, e);
            navigation = "customerError";
        }
        return navigation;
    }

    public List<Customer> retrieveAllCustomers() {
        try {
            ResponseEntity<List<Customer>> response = restTemplate.exchange(
                    BASE_URL + "/all",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Customer>>() {});
            return response.getBody();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error retrieving all customers", e);
            return java.util.Collections.emptyList();
        }
    }
}
