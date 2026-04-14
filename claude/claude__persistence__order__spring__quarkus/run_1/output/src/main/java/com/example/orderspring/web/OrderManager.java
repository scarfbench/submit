package com.example.orderspring.web;

import com.example.orderspring.entity.CustomerOrder;
import com.example.orderspring.entity.LineItem;
import com.example.orderspring.entity.Part;
import com.example.orderspring.entity.Vendor;
import com.example.orderspring.service.OrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderManager {

    @Inject
    OrderService orderService;

    // ---- Orders ----

    @GET
    @Path("/orders")
    public Response getOrders() {
        List<CustomerOrder> orders = orderService.getAllOrders();
        List<Map<String, Object>> result = orders.stream().map(this::orderToMap).collect(Collectors.toList());
        return Response.ok(result).build();
    }

    @POST
    @Path("/orders")
    public Response createOrder(Map<String, Object> body) {
        try {
            Integer orderId = ((Number) body.get("orderId")).intValue();
            String statusStr = (String) body.getOrDefault("status", "N");
            char status = statusStr.charAt(0);
            int discount = body.containsKey("discount") ? ((Number) body.get("discount")).intValue() : 0;
            String shipmentInfo = (String) body.getOrDefault("shipmentInfo", "");
            orderService.createOrder(orderId, status, discount, shipmentInfo);
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "Order created", "orderId", orderId))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/orders/{orderId}")
    public Response deleteOrder(@PathParam("orderId") Integer orderId) {
        try {
            orderService.removeOrder(orderId);
            return Response.ok(Map.of("message", "Order deleted", "orderId", orderId)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // ---- Line Items ----

    @GET
    @Path("/orders/{orderId}/lineItems")
    public Response getLineItems(@PathParam("orderId") Integer orderId) {
        List<LineItem> items = orderService.getLineItemsByOrderId(orderId);
        List<Map<String, Object>> result = items.stream().map(this::lineItemToMap).collect(Collectors.toList());
        return Response.ok(result).build();
    }

    @POST
    @Path("/orders/{orderId}/lineItems")
    public Response addLineItem(@PathParam("orderId") Integer orderId, Map<String, Object> body) {
        try {
            String partNumber = (String) body.get("partNumber");
            int revision = ((Number) body.get("revision")).intValue();
            int quantity = body.containsKey("quantity") ? ((Number) body.get("quantity")).intValue() : 1;
            orderService.addLineItem(orderId, partNumber, revision, quantity);
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "Line item added"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // ---- Parts ----

    @GET
    @Path("/parts")
    public Response getParts() {
        List<Part> parts = orderService.getAllParts();
        List<Map<String, Object>> result = parts.stream().map(this::partToMap).collect(Collectors.toList());
        return Response.ok(result).build();
    }

    // ---- Vendors ----

    @GET
    @Path("/vendors")
    public Response findVendors(@QueryParam("name") String name) {
        if (name == null || name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "name query parameter is required"))
                    .build();
        }
        List<Vendor> vendors = orderService.findVendorsByName(name);
        List<Map<String, Object>> result = vendors.stream().map(this::vendorToMap).collect(Collectors.toList());
        return Response.ok(result).build();
    }

    // ---- Aggregate queries ----

    @GET
    @Path("/stats/avgPrice")
    public Response getAvgPrice() {
        Double avg = orderService.getAvgPrice();
        return Response.ok(Map.of("averagePrice", avg != null ? avg : 0.0)).build();
    }

    @GET
    @Path("/stats/totalPrice/{vendorId}")
    public Response getTotalPricePerVendor(@PathParam("vendorId") int vendorId) {
        Double total = orderService.getTotalPricePerVendor(vendorId);
        return Response.ok(Map.of("vendorId", vendorId, "totalPrice", total != null ? total : 0.0)).build();
    }

    @GET
    @Path("/stats/itemCount")
    public Response getItemCount() {
        int count = orderService.countAllItems();
        return Response.ok(Map.of("totalItems", count)).build();
    }

    @GET
    @Path("/orders/{orderId}/price")
    public Response getOrderPrice(@PathParam("orderId") Integer orderId) {
        double price = orderService.getOrderPrice(orderId);
        return Response.ok(Map.of("orderId", orderId, "price", price)).build();
    }

    @GET
    @Path("/orders/{orderId}/vendors")
    public Response getVendorsByOrder(@PathParam("orderId") Integer orderId) {
        String report = orderService.reportVendorsByOrder(orderId);
        return Response.ok(Map.of("orderId", orderId, "vendorReport", report)).build();
    }

    // ---- Health check ----

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok(Map.of("status", "UP")).build();
    }

    // ---- DTO mapping helpers (avoid circular references in JSON) ----

    private Map<String, Object> orderToMap(CustomerOrder order) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getOrderId());
        map.put("status", String.valueOf(order.getStatus()));
        map.put("discount", order.getDiscount());
        map.put("shipmentInfo", order.getShipmentInfo());
        map.put("lastUpdate", order.getLastUpdate() != null ? order.getLastUpdate().toString() : null);
        return map;
    }

    private Map<String, Object> lineItemToMap(LineItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("itemId", item.getItemId());
        map.put("quantity", item.getQuantity());
        if (item.getVendorPart() != null) {
            map.put("vendorPartNumber", item.getVendorPart().getVendorPartNumber());
            map.put("vendorPartDescription", item.getVendorPart().getDescription());
            map.put("vendorPartPrice", item.getVendorPart().getPrice());
        }
        return map;
    }

    private Map<String, Object> partToMap(Part part) {
        Map<String, Object> map = new HashMap<>();
        map.put("partNumber", part.getPartNumber());
        map.put("revision", part.getRevision());
        map.put("description", part.getDescription());
        if (part.getVendorPart() != null) {
            map.put("vendorPartNumber", part.getVendorPart().getVendorPartNumber());
        }
        return map;
    }

    private Map<String, Object> vendorToMap(Vendor vendor) {
        Map<String, Object> map = new HashMap<>();
        map.put("vendorId", vendor.getVendorId());
        map.put("name", vendor.getName());
        map.put("address", vendor.getAddress());
        map.put("contact", vendor.getContact());
        map.put("phone", vendor.getPhone());
        return map;
    }
}
