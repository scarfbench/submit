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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<Map<String, Object>> result = orders.stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", o.getOrderId());
            map.put("status", String.valueOf(o.getStatus()));
            map.put("discount", o.getDiscount());
            map.put("shipmentInfo", o.getShipmentInfo());
            map.put("lastUpdate", o.getLastUpdate() != null ? o.getLastUpdate().toString() : null);
            return map;
        }).collect(Collectors.toList());
        return Response.ok(result).build();
    }

    @POST
    @Path("/orders")
    public Response createOrder(Map<String, Object> payload) {
        Integer orderId = (Integer) payload.get("orderId");
        String statusStr = payload.getOrDefault("status", "N").toString();
        char status = statusStr.charAt(0);
        int discount = payload.containsKey("discount") ? ((Number) payload.get("discount")).intValue() : 0;
        String shipmentInfo = (String) payload.getOrDefault("shipmentInfo", "");
        orderService.createOrder(orderId, status, discount, shipmentInfo);
        Map<String, String> result = new HashMap<>();
        result.put("message", "Order created");
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @DELETE
    @Path("/orders/{orderId}")
    public Response deleteOrder(@PathParam("orderId") Integer orderId) {
        orderService.removeOrder(orderId);
        return Response.noContent().build();
    }

    @GET
    @Path("/orders/{orderId}/lineitems")
    public Response getLineItems(@PathParam("orderId") Integer orderId) {
        List<LineItem> items = orderService.getLineItemsByOrderId(orderId);
        List<Map<String, Object>> result = items.stream().map(li -> {
            Map<String, Object> map = new HashMap<>();
            map.put("itemId", li.getItemId());
            map.put("quantity", li.getQuantity());
            if (li.getVendorPart() != null) {
                map.put("vendorPartNumber", li.getVendorPart().getVendorPartNumber());
            }
            return map;
        }).collect(Collectors.toList());
        return Response.ok(result).build();
    }

    @GET
    @Path("/orders/{orderId}/price")
    public Response getOrderPrice(@PathParam("orderId") Integer orderId) {
        double price = orderService.getOrderPrice(orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("price", price);
        return Response.ok(result).build();
    }

    @GET
    @Path("/orders/{orderId}/vendors")
    public Response getVendorsByOrder(@PathParam("orderId") Integer orderId) {
        String report = orderService.reportVendorsByOrder(orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("report", report);
        return Response.ok(result).build();
    }

    @GET
    @Path("/parts")
    public Response getAllParts() {
        List<Part> parts = orderService.getAllParts();
        List<Map<String, Object>> result = parts.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("partNumber", p.getPartNumber());
            map.put("revision", p.getRevision());
            map.put("description", p.getDescription());
            return map;
        }).collect(Collectors.toList());
        return Response.ok(result).build();
    }

    @GET
    @Path("/vendors/search")
    public Response searchVendors(@QueryParam("name") String name) {
        List<Vendor> vendors = orderService.findVendorsByName(name);
        List<Map<String, Object>> result = vendors.stream().map(v -> {
            Map<String, Object> map = new HashMap<>();
            map.put("vendorId", v.getVendorId());
            map.put("name", v.getName());
            map.put("address", v.getAddress());
            map.put("contact", v.getContact());
            map.put("phone", v.getPhone());
            return map;
        }).collect(Collectors.toList());
        return Response.ok(result).build();
    }

    @GET
    @Path("/vendorparts/avgprice")
    public Response getAveragePrice() {
        Double avg = orderService.getAvgPrice();
        Map<String, Object> result = new HashMap<>();
        result.put("averagePrice", avg);
        return Response.ok(result).build();
    }

    @GET
    @Path("/vendorparts/totalprice/{vendorId}")
    public Response getTotalPricePerVendor(@PathParam("vendorId") int vendorId) {
        Double total = orderService.getTotalPricePerVendor(vendorId);
        Map<String, Object> result = new HashMap<>();
        result.put("totalPrice", total);
        return Response.ok(result).build();
    }
}
