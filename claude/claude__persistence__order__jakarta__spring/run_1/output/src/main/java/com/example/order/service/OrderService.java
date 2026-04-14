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
package com.example.order.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order.entity.CustomerOrder;
import com.example.order.entity.LineItem;
import com.example.order.entity.Part;
import com.example.order.entity.PartKey;
import com.example.order.entity.Vendor;
import com.example.order.entity.VendorPart;
import com.example.order.repository.CustomerOrderRepository;
import com.example.order.repository.LineItemRepository;
import com.example.order.repository.PartRepository;
import com.example.order.repository.VendorPartRepository;
import com.example.order.repository.VendorRepository;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = Logger.getLogger("order.service.OrderService");

    private final CustomerOrderRepository orderRepository;
    private final PartRepository partRepository;
    private final VendorRepository vendorRepository;
    private final VendorPartRepository vendorPartRepository;
    private final LineItemRepository lineItemRepository;

    public OrderService(CustomerOrderRepository orderRepository,
                        PartRepository partRepository,
                        VendorRepository vendorRepository,
                        VendorPartRepository vendorPartRepository,
                        LineItemRepository lineItemRepository) {
        this.orderRepository = orderRepository;
        this.partRepository = partRepository;
        this.vendorRepository = vendorRepository;
        this.vendorPartRepository = vendorPartRepository;
        this.lineItemRepository = lineItemRepository;
    }

    public void createPart(String partNumber, int revision, String description,
                           java.util.Date revisionDate, String specification,
                           Serializable drawing) {
        Part part = new Part(partNumber, revision, description, revisionDate,
                specification, drawing);
        logger.log(Level.INFO, "Created part {0}-{1}", new Object[]{partNumber, revision});
        partRepository.save(part);
        logger.log(Level.INFO, "Persisted part {0}-{1}", new Object[]{partNumber, revision});
    }

    public List<Part> getAllParts() {
        return partRepository.findAllByOrderByPartNumberAsc();
    }

    public void addPartToBillOfMaterial(String bomPartNumber, int bomRevision,
                                        String partNumber, int revision) {
        logger.log(Level.INFO, "BOM part number: {0}", bomPartNumber);
        logger.log(Level.INFO, "BOM revision: {0}", bomRevision);
        logger.log(Level.INFO, "Part number: {0}", partNumber);
        logger.log(Level.INFO, "Part revision: {0}", revision);

        PartKey bomKey = new PartKey(bomPartNumber, bomRevision);
        Part bom = partRepository.findById(bomKey).orElseThrow();
        logger.log(Level.INFO, "BOM Part found: {0}", bom.getPartNumber());

        PartKey partKey = new PartKey(partNumber, revision);
        Part part = partRepository.findById(partKey).orElseThrow();
        logger.log(Level.INFO, "Part found: {0}", part.getPartNumber());

        bom.getParts().add(part);
        part.setBomPart(bom);
    }

    public void createVendor(int vendorId, String name, String address,
                             String contact, String phone) {
        Vendor vendor = new Vendor(vendorId, name, address, contact, phone);
        vendorRepository.save(vendor);
    }

    public void createVendorPart(String partNumber, int revision,
                                 String description, double price, int vendorId) {
        PartKey pkey = new PartKey(partNumber, revision);
        Part part = partRepository.findById(pkey).orElseThrow();

        VendorPart vendorPart = new VendorPart(description, price, part);
        vendorPartRepository.save(vendorPart);

        Vendor vendor = vendorRepository.findById(vendorId).orElseThrow();
        vendor.addVendorPart(vendorPart);
        vendorPart.setVendor(vendor);
    }

    public void createOrder(Integer orderId, char status, int discount, String shipmentInfo) {
        CustomerOrder order = new CustomerOrder(orderId, status, discount, shipmentInfo);
        orderRepository.save(order);
    }

    public List<CustomerOrder> getOrders() {
        return orderRepository.findAllByOrderByOrderIdAsc();
    }

    public void addLineItem(Integer orderId, String partNumber, int revision, int quantity) {
        CustomerOrder order = orderRepository.findById(orderId).orElseThrow();
        logger.log(Level.INFO, "Found order ID {0}", orderId);

        PartKey pkey = new PartKey(partNumber, revision);
        Part part = partRepository.findById(pkey).orElseThrow();

        LineItem lineItem = new LineItem(order, quantity, part.getVendorPart());
        order.addLineItem(lineItem);
        orderRepository.save(order);
    }

    public double getBillOfMaterialPrice(String bomPartNumber, int bomRevision) {
        double price = 0.0;
        PartKey bomkey = new PartKey(bomPartNumber, bomRevision);
        Part bom = partRepository.findById(bomkey).orElseThrow();
        Collection<Part> parts = bom.getParts();
        for (Part part : parts) {
            VendorPart vendorPart = part.getVendorPart();
            price += vendorPart.getPrice();
        }
        return price;
    }

    public double getOrderPrice(Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId).orElseThrow();
        return order.calculateAmmount();
    }

    public void adjustOrderDiscount(int adjustment) {
        List<CustomerOrder> orders = orderRepository.findAllByOrderByOrderIdAsc();
        for (CustomerOrder order : orders) {
            int newDiscount = order.getDiscount() + adjustment;
            order.setDiscount(Math.max(newDiscount, 0));
        }
    }

    public Double getAvgPrice() {
        return vendorPartRepository.findAveragePrice();
    }

    public Double getTotalPricePerVendor(int vendorId) {
        return vendorPartRepository.findTotalPriceByVendorId(vendorId);
    }

    public List<String> locateVendorsByPartialName(String name) {
        List<Vendor> vendors = vendorRepository.findByPartialName(name);
        return vendors.stream().map(Vendor::getName).collect(Collectors.toList());
    }

    public int countAllItems() {
        return (int) lineItemRepository.count();
    }

    public List<LineItem> getLineItems(int orderId) {
        return lineItemRepository.findByOrderId(orderId);
    }

    public void removeOrder(Integer orderId) {
        orderRepository.deleteById(orderId);
    }

    public String reportVendorsByOrder(Integer orderId) {
        StringBuilder report = new StringBuilder();
        List<Vendor> vendors = vendorRepository.findVendorsByOrderId(orderId);
        for (Vendor vendor : vendors) {
            report.append(vendor.getVendorId()).append(' ')
                  .append(vendor.getName()).append(' ')
                  .append(vendor.getContact()).append('\n');
        }
        return report.toString();
    }
}
