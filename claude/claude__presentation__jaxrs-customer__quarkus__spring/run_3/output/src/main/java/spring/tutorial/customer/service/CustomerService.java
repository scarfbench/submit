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
package spring.tutorial.customer.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.tutorial.customer.data.Address;
import spring.tutorial.customer.data.Customer;
import spring.tutorial.customer.repository.CustomerRepository;

/**
 * Customer Service for business logic
 */
@Service
@Transactional
public class CustomerService {

    private static final Logger logger =
            Logger.getLogger(CustomerService.class.getName());

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Simple persistence method
     *
     * @param customer
     * @return customerId long
     */
    public long persist(Customer customer) {
        try {
            Customer savedCustomer = customerRepository.save(customer);
            return savedCustomer.getId();
        } catch (Exception ex) {
            logger.warning("Something went wrong when persisting the customer: " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Simple query method to find Customer by ID.
     *
     * @param customerId
     * @return Customer
     */
    public Customer findById(String customerId) {
        Customer customer = null;
        try {
            int id = Integer.parseInt(customerId);
            customer = customerRepository.findById(id).orElse(null);
            return customer;
        } catch (Exception ex) {
            logger.log(Level.WARNING,
                    "Couldn't find customer with ID of {0}", customerId);
        }
        return customer;
    }

    /**
     * Find all customers
     *
     * @return List of customers
     */
    public List<Customer> findAllCustomers() {
        try {
            return customerRepository.findAllCustomersOrdered();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error when finding all customers: " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Simple remove method to remove a Customer
     *
     * @param customerId
     * @return boolean
     */
    public boolean remove(String customerId) {
        try {
            int id = Integer.parseInt(customerId);
            if (customerRepository.existsById(id)) {
                customerRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Couldn't remove customer with ID {0}: {1}",
                    new Object[]{customerId, ex.getMessage()});
            return false;
        }
    }
}
