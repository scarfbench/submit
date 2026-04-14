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
package jakarta.tutorial.order.web;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.tutorial.order.ejb.RequestBean;
import jakarta.tutorial.order.entity.CustomerOrder;
import jakarta.tutorial.order.entity.LineItem;
import jakarta.tutorial.order.entity.Part;


/**
 * REST resource replacing JSF OrderManager.
 * Exposes order management functionality via JAX-RS endpoints.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderManager {

    private static final Logger logger = Logger.getLogger("order.web.OrderManager");

    @Inject
    RequestBean request;

    @GET
    @Path("/orders")
    public List<CustomerOrder> getOrders() {
        try {
            return request.getOrders();
        } catch (Exception e) {
            logger.warning("Couldn't get orders.");
            return List.of();
        }
    }

    @GET
    @Path("/orders/{orderId}/lineItems")
    public List<LineItem> getLineItems(@PathParam("orderId") int orderId) {
        try {
            return request.getLineItems(orderId);
        } catch (Exception e) {
            logger.warning("Couldn't get lineItems.");
            return List.of();
        }
    }

    @DELETE
    @Path("/orders/{orderId}")
    public Response removeOrder(@PathParam("orderId") Integer orderId) {
        try {
            request.removeOrder(orderId);
            logger.log(Level.INFO, "Removed order {0}.", orderId);
            return Response.ok().build();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Problem removing order {0}", orderId);
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/orders")
    public Response submitOrder(OrderRequest orderReq) {
        try {
            request.createOrder(orderReq.orderId, orderReq.status, orderReq.discount,
                    orderReq.shipmentInfo);
            logger.log(Level.INFO, "Created new order with order ID {0}", orderReq.orderId);
            return Response.ok().build();
        } catch (Exception e) {
            logger.warning("Problem creating order in submitOrder.");
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/orders/{orderId}/lineItems")
    public Response addLineItem(@PathParam("orderId") Integer orderId, LineItemRequest lineItemReq) {
        try {
            request.addLineItem(orderId,
                    lineItemReq.partNumber,
                    lineItemReq.partRevision,
                    lineItemReq.quantity);
            logger.log(Level.INFO, "Adding line item to order # {0}", orderId);
            return Response.ok().build();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Problem adding line items to order ID {0}", orderId);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/parts")
    public List<Part> getAllParts() {
        return request.getAllParts();
    }

    @GET
    @Path("/vendors/search")
    public List<String> findVendor(@QueryParam("name") String vendorName) {
        try {
            return request.locateVendorsByPartialName(vendorName);
        } catch (Exception e) {
            logger.warning("Problem calling RequestBean.locateVendorsByPartialName from findVendor");
            return List.of();
        }
    }

    @GET
    @Path("/vendorparts/avgprice")
    public java.util.Map<String, Object> getAverageVendorPartPrice() {
        try {
            Double avgPrice = request.getAvgPrice();
            return java.util.Map.of("value", avgPrice);
        } catch (Exception e) {
            return java.util.Map.of("value", 0.0);
        }
    }

    @GET
    @Path("/vendors/{vendorId}/totalprice")
    public java.util.Map<String, Object> getTotalPricePerVendor(@PathParam("vendorId") int vendorId) {
        try {
            Double totalPrice = request.getTotalPricePerVendor(vendorId);
            return java.util.Map.of("value", totalPrice);
        } catch (Exception e) {
            return java.util.Map.of("value", 0.0);
        }
    }

    @GET
    @Path("/orders/{orderId}/price")
    public java.util.Map<String, Object> getOrderPrice(@PathParam("orderId") Integer orderId) {
        try {
            double price = request.getOrderPrice(orderId);
            return java.util.Map.of("value", price);
        } catch (Exception e) {
            return java.util.Map.of("value", 0.0);
        }
    }

    @GET
    @Path("/lineItems/count")
    public java.util.Map<String, Object> countAllItems() {
        try {
            int count = request.countAllItems();
            return java.util.Map.of("value", count);
        } catch (Exception e) {
            return java.util.Map.of("value", 0);
        }
    }

    @GET
    @Path("/orders/{orderId}/vendors")
    public Response reportVendorsByOrder(@PathParam("orderId") Integer orderId) {
        try {
            String report = request.reportVendorsByOrder(orderId);
            return Response.ok(report).type(MediaType.TEXT_PLAIN).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    /**
     * Simple DTO for order creation requests.
     */
    public static class OrderRequest {
        public Integer orderId;
        public char status;
        public int discount;
        public String shipmentInfo;
    }

    /**
     * Simple DTO for line item requests.
     */
    public static class LineItemRequest {
        public String partNumber;
        public int partRevision;
        public int quantity;
    }
}
