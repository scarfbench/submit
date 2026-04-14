package io.quarkuscoffeeshop.coffeeshop.web;

import io.quarkuscoffeeshop.coffeeshop.counter.api.OrderService;
import io.quarkuscoffeeshop.coffeeshop.domain.commands.PlaceOrderCommand;
import io.quarkuscoffeeshop.coffeeshop.infrastructure.events.WebUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoffeeshopApiResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoffeeshopApiResource.class);

    @Inject
    OrderService orderService;

    @Inject
    Event<WebUpdateEvent> webUpdateEvent;

    @POST
    @Path("/order")
    @Transactional
    public Response placeOrder(final PlaceOrderCommand placeOrderCommand) {

        LOGGER.info("PlaceOrderCommand received: {}", placeOrderCommand);
        try {
            orderService.onOrderIn(placeOrderCommand);
            return Response.accepted().build();
        } catch (Exception e) {
            LOGGER.error("Error placing order", e);
            String msg = e.getMessage() != null ? e.getMessage().replace('\n', ' ').replace('\r', ' ') : "Unknown error";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + msg.replace("\"", "'") + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @POST
    @Path("/message")
    public void sendMessage(final String message) {
        LOGGER.debug("received message: {}", message);
        LOGGER.debug("sending to web-updates: {}", message);
        webUpdateEvent.fire(new WebUpdateEvent(message));
    }

}
