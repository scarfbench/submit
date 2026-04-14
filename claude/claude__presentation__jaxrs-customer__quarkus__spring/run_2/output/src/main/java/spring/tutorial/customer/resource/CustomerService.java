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
package spring.tutorial.customer.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import spring.tutorial.customer.data.Address;
import spring.tutorial.customer.data.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Customer Restful Service with CRUD methods
 *
 * @author markito
 */

@RestController
@RequestMapping("/webapi/Customer")
@Transactional
public class CustomerService {

    public static final Logger logger =
            Logger.getLogger(CustomerService.class.getCanonicalName());

    @PersistenceContext
    private EntityManager em;

    private CriteriaBuilder cb;

    @PostConstruct
    private void init() {
        cb = em.getCriteriaBuilder();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = null;
        try {
            customers = this.findAllCustomers();
            if (customers == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error calling findAllCustomers()",
                    new Object[]{ex.getMessage()});
        }
        return ResponseEntity.ok(customers);
    }

    /**
     * Get customer JSON
     *
     * @param customerId
     * @return Customer
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable("id") String customerId) {
        Customer customer = findById(customerId);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }


    /**
     * createCustomer method based on Customer type
     *
     * @param customer
     * @return ResponseEntity with URI for the Customer added
     * @see Customer.java
     */
    @PostMapping
    public ResponseEntity<Void> createCustomer(@RequestBody Customer customer) {

        try {
            long customerId = persist(customer);
            return ResponseEntity.created(URI.create("/" + customerId)).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "Error creating customer for customerId {0}. {1}",
                    new Object[]{customer.getId(), e.getMessage()});
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error creating customer", e);
        }
    }

    /**
     * Update a resource
     *
     * @param customer
     * @return ResponseEntity
     * @see Customer.java
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCustomer(@PathVariable("id") String customerId,
            @RequestBody Customer customer) {

        try {
            Customer oldCustomer = findById(customerId);

            if (oldCustomer == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            } else {
                persist(customer);
                return ResponseEntity.status(HttpStatus.SEE_OTHER).build();
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error updating customer", e);
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
            if (!remove(customerId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error calling deleteCustomer() for customerId {0}. {1}",
                    new Object[]{customerId, ex.getMessage()});
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting customer", ex);
        }
    }

    /**
     * Simple persistence method
     *
     * @param customer
     * @return customerId long
     */
    private long persist(Customer customer) {

        try {
            Address address = customer.getAddress();
            em.persist(address);
            em.persist(customer);
        } catch (Exception ex) {
            logger.warning("Something went wrong when persisting the customer");
        }

        return customer.getId();
    }

    /**
     * Simple query method to find Customer by ID.
     *
     * @param customerId
     * @return Customer
     */
    private Customer findById(String customerId) {
        Customer customer = null;
        try {
            int id = Integer.parseInt(customerId);
            customer = em.find(Customer.class, id);
            return customer;
        } catch (Exception ex) {
            logger.log(Level.WARNING,
                    "Couldn't find customer with ID of {0}", customerId);
        }
        return customer;
    }

    private List<Customer> findAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try {
            customers = (List<Customer>) em.createNamedQuery("findAllCustomers").getResultList();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error when finding all customers");
        }
        return customers;
    }

    /**
     * Simple remove method to remove a Customer
     *
     * @param customerId
     * @return boolean
     */
    private boolean remove(String customerId) {
        Customer customer;
        try {
            int id = Integer.parseInt(customerId);
            customer = em.find(Customer.class, id);
            if (customer != null) {
                Address address = customer.getAddress();
                em.remove(address);
                em.remove(customer);
                return true;
            }
            return false;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Couldn't remove customer with ID {0}", customerId);
            return false;
        }
    }
}
