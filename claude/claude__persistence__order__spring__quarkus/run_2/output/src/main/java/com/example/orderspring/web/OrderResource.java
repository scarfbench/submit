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

import java.util.*;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderService orderService;

    @GET
    @Path("/health")
    public Response health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        return Response.ok(result).build();
    }

    @GET
    @Path("/orders")
    public Response getOrders() {
        List<CustomerOrder> orders = orderService.getAllOrders();
        List<Map<String, Object>> result = new ArrayList<>();
        for (CustomerOrder order : orders) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("orderId", order.getOrderId());
            map.put("status", String.valueOf(order.getStatus()));
            map.put("discount", order.getDiscount());
            map.put("shipmentInfo", order.getShipmentInfo());
            map.put("lastUpdate", order.getLastUpdate() != null ? order.getLastUpdate().toString() : null);
            result.add(map);
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/parts")
    public Response getParts() {
        List<Part> parts = orderService.getAllParts();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Part part : parts) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("partNumber", part.getPartNumber());
            map.put("revision", part.getRevision());
            map.put("description", part.getDescription());
            result.add(map);
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/orders/{orderId}/lineitems")
    public Response getLineItems(@PathParam("orderId") int orderId) {
        List<LineItem> items = orderService.getLineItems(orderId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (LineItem item : items) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("itemId", item.getItemId());
            map.put("quantity", item.getQuantity());
            if (item.getVendorPart() != null) {
                map.put("vendorPartNumber", item.getVendorPart().getVendorPartNumber());
            }
            result.add(map);
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/vendorparts/avgprice")
    public Response getAvgPrice() {
        Double avg = orderService.getAvgPrice();
        Map<String, Object> result = new HashMap<>();
        result.put("averagePrice", avg);
        return Response.ok(result).build();
    }

    @GET
    @Path("/vendorparts/totalprice/{vendorId}")
    public Response getTotalPrice(@PathParam("vendorId") int vendorId) {
        Double total = orderService.getTotalPricePerVendor(vendorId);
        Map<String, Object> result = new HashMap<>();
        result.put("totalPrice", total);
        return Response.ok(result).build();
    }

    @GET
    @Path("/vendors/search")
    public Response searchVendors(@QueryParam("name") String name) {
        List<Vendor> vendors = orderService.findVendorsByName(name);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Vendor vendor : vendors) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("vendorId", vendor.getVendorId());
            map.put("name", vendor.getName());
            map.put("address", vendor.getAddress());
            map.put("contact", vendor.getContact());
            map.put("phone", vendor.getPhone());
            result.add(map);
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/lineitems/count")
    public Response countLineItems() {
        int count = orderService.countAllItems();
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Response.ok(result).build();
    }

    @GET
    @Path("/orders/{orderId}/price")
    public Response getOrderPrice(@PathParam("orderId") int orderId) {
        double price = orderService.getOrderPrice(orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("price", price);
        return Response.ok(result).build();
    }

    @GET
    @Path("/orders/{orderId}/vendors")
    public Response getVendorsByOrder(@PathParam("orderId") int orderId) {
        String report = orderService.reportVendorsByOrder(orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("report", report);
        return Response.ok(result).build();
    }

    @POST
    @Path("/orders")
    public Response createOrder(Map<String, Object> body) {
        try {
            Integer orderId = body.get("orderId") instanceof Number ? ((Number) body.get("orderId")).intValue() : Integer.parseInt(body.get("orderId").toString());
            String statusStr = body.get("status") != null ? body.get("status").toString() : "N";
            char status = statusStr.length() > 0 ? statusStr.charAt(0) : 'N';
            int discount = body.get("discount") instanceof Number ? ((Number) body.get("discount")).intValue() : 0;
            String shipmentInfo = body.get("shipmentInfo") != null ? body.get("shipmentInfo").toString() : "";

            orderService.createOrder(orderId, status, discount, shipmentInfo);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    @DELETE
    @Path("/orders/{orderId}")
    public Response deleteOrder(@PathParam("orderId") int orderId) {
        try {
            orderService.removeOrder(orderId);
            return Response.noContent().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
