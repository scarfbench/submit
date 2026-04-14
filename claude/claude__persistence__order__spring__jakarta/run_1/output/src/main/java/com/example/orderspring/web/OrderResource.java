package com.example.orderspring.web;

import com.example.orderspring.entity.CustomerOrder;
import com.example.orderspring.entity.LineItem;
import com.example.orderspring.entity.Part;
import com.example.orderspring.entity.Vendor;
import com.example.orderspring.service.OrderService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

import java.util.List;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    @EJB
    private OrderService orderService;

    @GET
    public Response getAllOrders() {
        List<CustomerOrder> orders = orderService.getAllOrders();
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (CustomerOrder order : orders) {
            JsonObjectBuilder obj = Json.createObjectBuilder()
                .add("orderId", order.getOrderId())
                .add("status", String.valueOf(order.getStatus()))
                .add("discount", order.getDiscount())
                .add("shipmentInfo", order.getShipmentInfo() != null ? order.getShipmentInfo() : "");
            array.add(obj);
        }
        return Response.ok(array.build().toString()).build();
    }

    @GET
    @Path("/{orderId}")
    public Response getOrder(@PathParam("orderId") Integer orderId) {
        var orderOpt = orderService.getOrders().stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst();
        if (orderOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        CustomerOrder order = orderOpt.get();
        JsonObjectBuilder obj = Json.createObjectBuilder()
            .add("orderId", order.getOrderId())
            .add("status", String.valueOf(order.getStatus()))
            .add("discount", order.getDiscount())
            .add("shipmentInfo", order.getShipmentInfo() != null ? order.getShipmentInfo() : "");
        return Response.ok(obj.build().toString()).build();
    }

    @GET
    @Path("/{orderId}/lineitems")
    public Response getLineItems(@PathParam("orderId") Integer orderId) {
        List<LineItem> items = orderService.getLineItemsByOrderId(orderId);
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (LineItem item : items) {
            JsonObjectBuilder obj = Json.createObjectBuilder()
                .add("itemId", item.getItemId())
                .add("quantity", item.getQuantity());
            if (item.getVendorPart() != null) {
                obj.add("vendorPartNumber", item.getVendorPart().getVendorPartNumber());
            }
            array.add(obj);
        }
        return Response.ok(array.build().toString()).build();
    }

    @GET
    @Path("/parts")
    public Response getAllParts() {
        List<Part> parts = orderService.getAllParts();
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (Part part : parts) {
            JsonObjectBuilder obj = Json.createObjectBuilder()
                .add("partNumber", part.getPartNumber())
                .add("revision", part.getRevision())
                .add("description", part.getDescription() != null ? part.getDescription() : "");
            array.add(obj);
        }
        return Response.ok(array.build().toString()).build();
    }

    @GET
    @Path("/vendors/search")
    public Response searchVendors(@QueryParam("name") String name) {
        List<Vendor> vendors = orderService.findVendorsByName(name);
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (Vendor vendor : vendors) {
            JsonObjectBuilder obj = Json.createObjectBuilder()
                .add("vendorId", vendor.getVendorId())
                .add("name", vendor.getName() != null ? vendor.getName() : "")
                .add("contact", vendor.getContact() != null ? vendor.getContact() : "");
            array.add(obj);
        }
        return Response.ok(array.build().toString()).build();
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok("{\"status\":\"UP\"}").build();
    }
}
