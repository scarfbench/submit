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
package spring.tutorial.customer.controller;

import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import spring.tutorial.customer.data.Customer;
import spring.tutorial.customer.service.CustomerService;

/**
 * Customer Restful Controller with CRUD methods
 *
 * @author markito
 */
@RestController
@RequestMapping("/webapi/Customer")
public class CustomerRestController {

    public static final Logger logger =
            Logger.getLogger(CustomerRestController.class.getCanonicalName());

    @Autowired
    private CustomerService customerService;

    @GetMapping("/all")
    public List<Customer> getAllCustomers() {
        try {
            List<Customer> customers = customerService.findAllCustomers();
            if (customers == null || customers.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No customers found");
            }
            return customers;
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error calling findAllCustomers()",
                    new Object[]{ex.getMessage()});
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving customers", ex);
        }
    }

    /**
     * Get customer JSON
     *
     * @param customerId
     * @return Customer
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable("id") String customerId) {
        Customer customer = customerService.findById(customerId);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }

    /**
     * createCustomer method based on Customer
     *
     * @param customer
     * @return Response URI for the Customer added
     * @see Customer.java
     */
    @PostMapping
    public ResponseEntity<Void> createCustomer(@RequestBody Customer customer) {
        try {
            long customerId = customerService.persist(customer);
            return ResponseEntity.created(URI.create("/" + customerId)).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "Error creating customer for customerId {0}. {1}",
                    new Object[]{customer.getId(), e.getMessage()});
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating customer", e);
        }
    }

    /**
     * Update a resource
     *
     * @param customer
     * @return Response URI for the Customer added
     * @see Customer.java
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCustomer(@PathVariable("id") String customerId,
            @RequestBody Customer customer) {
        try {
            Customer oldCustomer = customerService.findById(customerId);

            if (oldCustomer == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            } else {
                customerService.persist(customer);
                return ResponseEntity.status(HttpStatus.SEE_OTHER).build();
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating customer", e);
        }
    }

    /**
     * Delete a resource
     *
     * @param customerId
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable("id") String customerId) {
        try {
            if (!customerService.remove(customerId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error calling deleteCustomer() for customerId {0}. {1}",
                    new Object[]{customerId, ex.getMessage()});
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting customer", ex);
        }
    }
}
