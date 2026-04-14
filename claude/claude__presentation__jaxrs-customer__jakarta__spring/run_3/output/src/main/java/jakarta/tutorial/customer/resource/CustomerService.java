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
package jakarta.tutorial.customer.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.tutorial.customer.data.Address;
import jakarta.tutorial.customer.data.Customer;

/**
 * Customer Restful Service with CRUD methods
 *
 * @author markito
 */
@RestController
@RequestMapping("/webapi/Customer")
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
    
    @GetMapping(path = "/all", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Customer>> getAllCustomers() {
        try {
            List<Customer> customers = this.findAllCustomers();
            if (customers == null || customers.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No customers found");
            }
            return ResponseEntity.ok(customers);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error calling findAllCustomers()",
                    new Object[]{ex.getMessage()});
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving customers", ex);
        }
    }

    /**
     * Get customer XML
     *
     * @param customerId
     * @return Customer
     */
    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Customer> getCustomer(@PathVariable("id") String customerId) {
        try {
            Customer customer = findById(customerId);
            if (customer == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }
            return ResponseEntity.ok(customer);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error calling findCustomer() for customerId {0}. {1}",
                    new Object[]{customerId, ex.getMessage()});
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving customer", ex);
        }
    }

    /**
     * createCustomer method based on
     * <code>CustomerType</code>
     *
     * @param customer
     * @return Response URI for the Customer added
     * @see Customer.java
     */
    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> createCustomer(@RequestBody Customer customer) {
        try {
            long customerId = persist(customer);
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
    @PutMapping(path = "/{id}", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> updateCustomer(@PathVariable("id") String customerId,
            @RequestBody Customer customer) {
        try {
            Customer oldCustomer = findById(customerId);

            if (oldCustomer == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            } else {
                persist(customer);
                return ResponseEntity.status(HttpStatus.SEE_OTHER).build();
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating customer", e);
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
            if (!remove(customerId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error calling deleteCustomer() for customerId {0}. {1}",
                    new Object[]{customerId, ex.getMessage()});
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting customer", ex);
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
     * @throws IOException
     */
    private Customer findById(String customerId) {
        Customer customer = null;
        try {
            int id = Integer.parseInt(customerId);
            customer = em.find(Customer.class, id);
            return customer;
        } catch (NumberFormatException ex) {
            logger.log(Level.WARNING, 
                    "Invalid customer ID format: {0}", customerId);
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
     * @throws IOException
     */
    private boolean remove(String customerId) {
        Customer customer;
        try {
            int id = Integer.parseInt(customerId);
            customer = em.find(Customer.class, id);
            if (customer == null) {
                logger.log(Level.WARNING, "Customer with ID {0} not found", customerId);
                return false;
            }
            Address address = customer.getAddress();
            em.remove(address);
            em.remove(customer);
            return true;
        } catch (NumberFormatException ex) {
            logger.log(Level.WARNING, "Invalid customer ID format: {0}", customerId);
            return false;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Couldn't remove customer with ID {0}", customerId);
            return false;
        }
    }
}
