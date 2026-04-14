package com.coffeeshop.web.api;

import com.coffeeshop.common.commands.PlaceOrderCommand;
import com.coffeeshop.common.domain.Order;
import com.coffeeshop.common.events.OrderEventResult;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoffeeshopApiResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoffeeshopApiResource.class);

    /**
     * Accept a PlaceOrderCommand and return the computed OrderEventResult.
     * No DB or Kafka yet; purely domain logic from the common module.
     */
    @POST
    @Path("/order")
    public Response placeOrder(PlaceOrderCommand placeOrderCommand) {
        LOGGER.info("PlaceOrderCommand received: {}", placeOrderCommand);
        OrderEventResult result = Order.from(placeOrderCommand);
        return Response.accepted(result).build();
    }

    /**
     * Simple message endpoint to verify wiring. For now, just logs the message.
     */
    @POST
    @Path("/message")
    public Response sendMessage(String message) {
        LOGGER.debug("received message: {}", message);
        return Response.accepted().build();
    }

    @GET
    @Path("/health")
    @Produces(MediaType.TEXT_PLAIN)
    public String health() {
        return "web-service OK";
    }
}
