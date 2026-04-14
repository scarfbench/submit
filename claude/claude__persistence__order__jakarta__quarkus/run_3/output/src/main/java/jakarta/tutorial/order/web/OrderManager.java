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
import jakarta.tutorial.order.ejb.RequestBean;
import jakarta.tutorial.order.entity.CustomerOrder;
import jakarta.tutorial.order.entity.LineItem;
import jakarta.tutorial.order.entity.Part;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


/**
 * REST resource replacing the JSF OrderManager bean.
 * Exposes the order management functionality as REST endpoints.
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
            throw new WebApplicationException("Failed to get orders", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/orders/{orderId}/lineItems")
    public List<LineItem> getLineItems(@PathParam("orderId") int orderId) {
        try {
            return request.getLineItems(orderId);
        } catch (Exception e) {
            logger.warning("Couldn't get lineItems.");
            throw new WebApplicationException("Failed to get line items", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/orders/{orderId}")
    public Response removeOrder(@PathParam("orderId") Integer orderId) {
        try {
            request.removeOrder(orderId);
            logger.log(Level.INFO, "Removed order {0}.", orderId);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error removing order {0}", orderId);
            throw new WebApplicationException("Failed to remove order", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/orders")
    public Response submitOrder(OrderRequest orderRequest) {
        try {
            request.createOrder(orderRequest.orderId, orderRequest.status, orderRequest.discount,
                    orderRequest.shipmentInfo);
            logger.log(Level.INFO, "Created new order with order ID {0}, status {1}, "
                    + "discount {2}, and shipping info {3}.",
                    new Object[]{orderRequest.orderId, orderRequest.status,
                            orderRequest.discount, orderRequest.shipmentInfo});
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            logger.warning("Problem creating order in submitOrder.");
            throw new WebApplicationException("Failed to create order", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/orders/{orderId}/lineItems")
    public Response addLineItem(@PathParam("orderId") Integer orderId, LineItemRequest lineItemRequest) {
        try {
            request.addLineItem(orderId,
                    lineItemRequest.partNumber,
                    lineItemRequest.partRevision,
                    lineItemRequest.quantity);
            logger.log(Level.INFO, "Adding line item to order # {0}", orderId);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Problem adding line items to order ID {0}", orderId);
            throw new WebApplicationException("Failed to add line item", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/parts")
    public List<Part> getParts() {
        return request.getAllParts();
    }

    @GET
    @Path("/vendors/search")
    public List<String> findVendor(@QueryParam("name") String name) {
        try {
            List<String> results = request.locateVendorsByPartialName(name);
            logger.log(Level.INFO, "Found {0} vendor(s) using the search string {1}.",
                    new Object[]{results.size(), name});
            return results;
        } catch (Exception e) {
            logger.warning("Problem calling RequestBean.locateVendorsByPartialName from findVendor");
            throw new WebApplicationException("Failed to search vendors", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/orders/{orderId}/price")
    public Response getOrderPrice(@PathParam("orderId") Integer orderId) {
        try {
            double price = request.getOrderPrice(orderId);
            return Response.ok(new PriceResponse(price)).build();
        } catch (Exception e) {
            throw new WebApplicationException("Failed to get order price", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/vendorparts/avgprice")
    public Response getAvgPrice() {
        try {
            Double avg = request.getAvgPrice();
            return Response.ok(new PriceResponse(avg != null ? avg : 0.0)).build();
        } catch (Exception e) {
            throw new WebApplicationException("Failed to get average price", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/vendors/{vendorId}/totalprice")
    public Response getTotalPricePerVendor(@PathParam("vendorId") int vendorId) {
        try {
            Double total = request.getTotalPricePerVendor(vendorId);
            return Response.ok(new PriceResponse(total != null ? total : 0.0)).build();
        } catch (Exception e) {
            throw new WebApplicationException("Failed to get total price", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/lineitems/count")
    public Response countAllItems() {
        try {
            int count = request.countAllItems();
            return Response.ok(new CountResponse(count)).build();
        } catch (Exception e) {
            throw new WebApplicationException("Failed to count items", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/orders/{orderId}/vendors")
    public Response reportVendorsByOrder(@PathParam("orderId") Integer orderId) {
        try {
            String report = request.reportVendorsByOrder(orderId);
            return Response.ok(new ReportResponse(report)).build();
        } catch (Exception e) {
            throw new WebApplicationException("Failed to get vendors report", Response.Status.INTERNAL_SERVER_ERROR);
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
