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
import jakarta.transaction.Transactional;
import jakarta.tutorial.order.ejb.RequestBean;
import jakarta.tutorial.order.entity.CustomerOrder;
import jakarta.tutorial.order.entity.LineItem;
import jakarta.tutorial.order.entity.Part;
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

/**
 * REST resource replacing the original JSF OrderManager bean.
 * Exposes all order-related operations as REST API endpoints.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderManager {

    @Inject
    RequestBean request;

    private static final Logger logger = Logger.getLogger("order.web.OrderManager");

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
    @Transactional
    public Response removeOrder(@PathParam("orderId") Integer orderId) {
        try {
            request.removeOrder(orderId);
            logger.log(Level.INFO, "Removed order {0}.", orderId);
            return Response.ok().build();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to remove order {0}.", orderId);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/vendors/search")
    public List<String> findVendor(@QueryParam("name") String vendorName) {
        try {
            List<String> results = request.locateVendorsByPartialName(vendorName);
            logger.log(Level.INFO, "Found {0} vendor(s) using the search string {1}.",
                    new Object[]{results.size(), vendorName});
            return results;
        } catch (Exception e) {
            logger.warning("Problem calling RequestBean.locateVendorsByPartialName from findVendor");
            return List.of();
        }
    }

    @POST
    @Path("/orders")
    @Transactional
    public Response submitOrder(OrderRequest orderRequest) {
        try {
            request.createOrder(orderRequest.orderId, orderRequest.status,
                    orderRequest.discount, orderRequest.shipmentInfo);

            logger.log(Level.INFO, "Created new order with order ID {0}, status {1}, "
                    + "discount {2}, and shipping info {3}.",
                    new Object[]{orderRequest.orderId, orderRequest.status,
                        orderRequest.discount, orderRequest.shipmentInfo});
            return Response.ok().build();
        } catch (Exception e) {
            logger.warning("Problem creating order in submitOrder.");
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/orders/{orderId}/lineItems")
    @Transactional
    public Response addLineItem(@PathParam("orderId") Integer orderId, LineItemRequest lineItemRequest) {
        try {
            request.addLineItem(orderId,
                    lineItemRequest.partNumber,
                    lineItemRequest.partRevision,
                    lineItemRequest.quantity);
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
    @Path("/orders/{orderId}/price")
    public Response getOrderPrice(@PathParam("orderId") Integer orderId) {
        try {
            double price = request.getOrderPrice(orderId);
            return Response.ok(new PriceResponse(price)).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/vendorparts/avgprice")
    public Response getAvgPrice() {
        try {
            Double avg = request.getAvgPrice();
            return Response.ok(new PriceResponse(avg != null ? avg : 0.0)).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/vendors/{vendorId}/totalprice")
    public Response getTotalPricePerVendor(@PathParam("vendorId") int vendorId) {
        try {
            Double total = request.getTotalPricePerVendor(vendorId);
            return Response.ok(new PriceResponse(total != null ? total : 0.0)).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/lineitems/count")
    public Response countAllItems() {
        try {
            int count = request.countAllItems();
            return Response.ok(new CountResponse(count)).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/vendors/{orderId}/report")
    public Response reportVendorsByOrder(@PathParam("orderId") Integer orderId) {
        try {
            String report = request.reportVendorsByOrder(orderId);
            return Response.ok(new ReportResponse(report)).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    // DTO classes for request/response
    public static class OrderRequest {
        public Integer orderId;
        public char status;
        public int discount;
        public String shipmentInfo;
    }

    public static class LineItemRequest {
        public String partNumber;
        public int partRevision;
        public int quantity;
    }

    public static class PriceResponse {
        public double price;
        public PriceResponse() {}
        public PriceResponse(double price) { this.price = price; }
    }

    public static class CountResponse {
        public int count;
        public CountResponse() {}
        public CountResponse(int count) { this.count = count; }
    }

    public static class ReportResponse {
        public String report;
        public ReportResponse() {}
        public ReportResponse(String report) { this.report = report; }
    }
}
