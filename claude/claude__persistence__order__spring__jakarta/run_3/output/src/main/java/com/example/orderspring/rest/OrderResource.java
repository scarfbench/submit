package com.example.orderspring.rest;

import com.example.orderspring.entity.*;
import com.example.orderspring.service.OrderService;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    private OrderService orderService;

    // --- Orders ---

    @GET
    @Path("orders")
    public Response getOrders() {
        List<CustomerOrder> orders = orderService.getAllOrders();
        JsonArrayBuilder ab = Json.createArrayBuilder();
        for (CustomerOrder o : orders) {
            ab.add(orderToJson(o));
        }
        return Response.ok(ab.build()).build();
    }

    @POST
    @Path("orders")
    public Response createOrder(JsonObject json) {
        int orderId = json.getInt("orderId");
        String statusStr = json.getString("status", "N");
        char status = statusStr.charAt(0);
        int discount = json.getInt("discount", 0);
        String shipmentInfo = json.getString("shipmentInfo", "");
        orderService.createOrder(orderId, status, discount, shipmentInfo);
        return Response.status(Response.Status.CREATED).entity(Json.createObjectBuilder().add("orderId", orderId).build()).build();
    }

    @DELETE
    @Path("orders/{orderId}")
    public Response deleteOrder(@PathParam("orderId") Integer orderId) {
        orderService.removeOrder(orderId);
        return Response.ok().build();
    }

    @GET
    @Path("orders/{orderId}/line-items")
    public Response getLineItems(@PathParam("orderId") Integer orderId) {
        List<LineItem> items = orderService.getLineItems(orderId);
        JsonArrayBuilder ab = Json.createArrayBuilder();
        for (LineItem li : items) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("itemId", li.getItemId());
            ob.add("quantity", li.getQuantity());
            if (li.getVendorPart() != null) {
                ob.add("vendorPartNumber", li.getVendorPart().getVendorPartNumber());
            }
            ab.add(ob);
        }
        return Response.ok(ab.build()).build();
    }

    @GET
    @Path("orders/{orderId}/price")
    public Response getOrderPrice(@PathParam("orderId") Integer orderId) {
        double price = orderService.getOrderPrice(orderId);
        return Response.ok(Json.createObjectBuilder().add("price", price).build()).build();
    }

    @GET
    @Path("orders/{orderId}/vendors")
    public Response getVendorsByOrder(@PathParam("orderId") Integer orderId) {
        String report = orderService.reportVendorsByOrder(orderId);
        return Response.ok(Json.createObjectBuilder().add("report", report).build()).build();
    }

    // --- Parts ---

    @GET
    @Path("parts")
    public Response getParts() {
        List<Part> parts = orderService.getAllParts();
        JsonArrayBuilder ab = Json.createArrayBuilder();
        for (Part p : parts) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("partNumber", p.getPartNumber());
            ob.add("revision", p.getRevision());
            ob.add("description", p.getDescription() != null ? p.getDescription() : "");
            ab.add(ob);
        }
        return Response.ok(ab.build()).build();
    }

    @GET
    @Path("parts/{partNumber}/{revision}/bom-price")
    public Response getBomPrice(@PathParam("partNumber") String partNumber,
                                @PathParam("revision") int revision) {
        double price = orderService.getBillOfMaterialPrice(partNumber, revision, partNumber, revision);
        return Response.ok(Json.createObjectBuilder().add("price", price).build()).build();
    }

    // --- Vendor Parts ---

    @GET
    @Path("vendor-parts")
    public Response getVendorParts() {
        List<Part> parts = orderService.getAllParts();
        JsonArrayBuilder ab = Json.createArrayBuilder();
        for (Part p : parts) {
            if (p.getVendorPart() != null) {
                VendorPart vp = p.getVendorPart();
                JsonObjectBuilder ob = Json.createObjectBuilder();
                ob.add("vendorPartNumber", vp.getVendorPartNumber());
                ob.add("description", vp.getDescription() != null ? vp.getDescription() : "");
                ob.add("price", vp.getPrice());
                ab.add(ob);
            }
        }
        return Response.ok(ab.build()).build();
    }

    @GET
    @Path("vendor-parts/avg-price")
    public Response getAvgPrice() {
        Double avg = orderService.getAvgPrice();
        return Response.ok(Json.createObjectBuilder().add("avgPrice", avg != null ? avg : 0.0).build()).build();
    }

    // --- Vendors ---

    @GET
    @Path("vendors/search")
    public Response searchVendors(@QueryParam("name") String name) {
        List<Vendor> vendors = orderService.findVendorsByName(name);
        JsonArrayBuilder ab = Json.createArrayBuilder();
        for (Vendor v : vendors) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("vendorId", v.getVendorId());
            ob.add("name", v.getName() != null ? v.getName() : "");
            ob.add("contact", v.getContact() != null ? v.getContact() : "");
            ab.add(ob);
        }
        return Response.ok(ab.build()).build();
    }

    @GET
    @Path("vendors/{vendorId}/total-price")
    public Response getTotalPricePerVendor(@PathParam("vendorId") int vendorId) {
        Double total = orderService.getTotalPricePerVendor(vendorId);
        return Response.ok(Json.createObjectBuilder().add("price", total != null ? total : 0.0).build()).build();
    }

    // --- Line Items ---

    @GET
    @Path("line-items/count")
    public Response countAllItems() {
        int count = orderService.countAllItems();
        return Response.ok(Json.createObjectBuilder().add("count", count).build()).build();
    }

    // Helper
    private JsonObjectBuilder orderToJson(CustomerOrder o) {
        JsonObjectBuilder ob = Json.createObjectBuilder();
        ob.add("orderId", o.getOrderId());
        ob.add("status", String.valueOf(o.getStatus()));
        ob.add("discount", o.getDiscount());
        ob.add("shipmentInfo", o.getShipmentInfo() != null ? o.getShipmentInfo() : "");
        if (o.getLastUpdate() != null) {
            ob.add("lastUpdate", o.getLastUpdate().toString());
        }
        return ob;
    }
}
