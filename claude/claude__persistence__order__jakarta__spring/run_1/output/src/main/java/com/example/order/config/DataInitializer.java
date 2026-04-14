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
package com.example.order.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.order.service.OrderService;

/**
 * Replaces the Jakarta EE @Singleton @Startup ConfigBean.
 * Initializes seed data on application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final OrderService orderService;

    public DataInitializer(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) {
        // Create parts
        orderService.createPart("1234-5678-01", 1, "ABC PART",
                new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        orderService.createPart("9876-4321-02", 2, "DEF PART",
                new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        orderService.createPart("5456-6789-03", 3, "GHI PART",
                new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        orderService.createPart("ABCD-XYZW-FF", 5, "XYZ PART",
                new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        orderService.createPart("SDFG-ERTY-BN", 7, "BOM PART",
                new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);

        // Add parts to bill of materials
        orderService.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "1234-5678-01", 1);
        orderService.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "9876-4321-02", 2);
        orderService.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "5456-6789-03", 3);
        orderService.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "ABCD-XYZW-FF", 5);

        // Create vendors
        orderService.createVendor(100, "WidgetCorp",
                "111 Main St., Anytown, KY 99999", "Mr. Jones", "888-777-9999");
        orderService.createVendor(200, "Gadget, Inc.",
                "123 State St., Sometown, MI 88888", "Mrs. Smith", "866-345-6789");

        // Create vendor parts
        orderService.createVendorPart("1234-5678-01", 1, "PART1", 100.00, 100);
        orderService.createVendorPart("9876-4321-02", 2, "PART2", 10.44, 200);
        orderService.createVendorPart("5456-6789-03", 3, "PART3", 76.23, 200);
        orderService.createVendorPart("ABCD-XYZW-FF", 5, "PART4", 55.19, 100);
        orderService.createVendorPart("SDFG-ERTY-BN", 7, "PART5", 345.87, 100);

        // Create orders and line items
        Integer orderId = 1111;
        orderService.createOrder(orderId, 'N', 10, "333 New Court, New City, CA 90000");
        orderService.addLineItem(orderId, "1234-5678-01", 1, 3);
        orderService.addLineItem(orderId, "9876-4321-02", 2, 5);
        orderService.addLineItem(orderId, "ABCD-XYZW-FF", 5, 7);

        orderId = 4312;
        orderService.createOrder(orderId, 'N', 0, "333 New Court, New City, CA 90000");
        orderService.addLineItem(orderId, "SDFG-ERTY-BN", 7, 1);
        orderService.addLineItem(orderId, "ABCD-XYZW-FF", 5, 3);
        orderService.addLineItem(orderId, "1234-5678-01", 1, 15);
    }
}
