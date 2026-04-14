package com.coffeeshop.counter.api;

import com.coffeeshop.common.commands.PlaceOrderCommand;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
public class CounterApiResource {

    private static final Logger log = LoggerFactory.getLogger(CounterApiResource.class);

    private final OrderService orderService;

    public CounterApiResource(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/order  (JSON -> PlaceOrderCommand)
    @POST
    @Path("/order")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response placeOrder(@Valid PlaceOrderCommand command) {
        log.info("POST /api/order: {}", command);
        orderService.onOrderIn(command);
        return Response.accepted().build();
    }

    // POST /api/order-up  (JSON as raw string)
    @POST
    @Path("/order-up")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response orderUpJson(String json) {
        log.info("POST /api/order-up (json): {}", json);
        orderService.onOrderUp(json);
        return Response.accepted().build();
    }

    // OPTIONAL: text/plain variant if you want to post a plain JSON string
    @POST
    @Path("/order-up")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response orderUpText(String body) {
        log.info("POST /api/order-up (text): {}", body);
        orderService.onOrderUp(body);
        return Response.accepted().build();
    }

    // Handy browser check
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "counter-service is running";
    }
}
