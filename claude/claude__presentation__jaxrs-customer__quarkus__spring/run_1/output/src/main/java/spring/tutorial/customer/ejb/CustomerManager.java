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

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import spring.tutorial.customer.data.Customer;
import spring.tutorial.customer.data.Address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Customer Manager for JSF backing bean
 *
 * @author ievans
 */
@Component("customerManager")
@Scope("request")
public class CustomerManager implements Serializable {
    private static final Logger logger = Logger.getLogger(CustomerManager.class.getName());

    private Customer customer = new Customer();
    private List<Customer> customers = java.util.Collections.emptyList();

    @Autowired
    private CustomerBean customerBean;

    @PostConstruct
    private void init() {
        logger.info("CustomerManager init");
        if (customer.getAddress() == null) {
            customer.setAddress(new Address());
        }
        try {
            customers = customerBean.retrieveAllCustomers();
        } catch (Exception e) {
            logger.warning("retrieveAllCustomers failed: " + e.getMessage());
            customers = java.util.Collections.emptyList();
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Customer> getCustomers() {
        try {
            return customerBean.retrieveAllCustomers();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
