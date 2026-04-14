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
package spring.tutorial.customer.web;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spring.tutorial.customer.client.CustomerClient;
import spring.tutorial.customer.data.Address;
import spring.tutorial.customer.data.Customer;

/**
 * Spring MVC Controller for Customer web interface
 *
 * @author ievans
 */
@Controller
public class CustomerController {
    private static final Logger logger = Logger.getLogger(CustomerController.class.getName());

    @Autowired
    private CustomerClient customerClient;

    @GetMapping("/")
    public String index(Model model) {
        Customer customer = new Customer();
        if (customer.getAddress() == null) {
            customer.setAddress(new Address());
        }
        model.addAttribute("customer", customer);
        return "index";
    }

    @GetMapping("/list")
    public String listCustomers(Model model) {
        try {
            List<Customer> customers = customerClient.retrieveAllCustomers();
            model.addAttribute("customers", customers);
        } catch (Exception e) {
            logger.warning("retrieveAllCustomers failed: " + e.getMessage());
            model.addAttribute("customers", Collections.emptyList());
        }
        return "list";
    }

    @PostMapping("/create")
    public String createCustomer(@ModelAttribute Customer customer, Model model) {
        if (customer == null) {
            logger.warning("customer is null.");
            return "error";
        }

        boolean success = customerClient.createCustomer(customer);
        if (success) {
            return "redirect:/list";
        } else {
            logger.warning("couldn't create customer with id " + customer.getId());
            model.addAttribute("errorMessage", "Could not create customer.");
            return "error";
        }
    }
}
