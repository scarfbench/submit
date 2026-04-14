package com.coffeeshop.orders.web;

import com.coffeeshop.common.domain.OrderRequest;
import com.coffeeshop.orders.domain.OrderEntity;
import com.coffeeshop.orders.domain.OrderRepository;
import com.coffeeshop.orders.messaging.OrdersPipeline;
import com.coffeeshop.orders.service.OrderService;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrdersResource {
    @Inject
    OrderService orderService;

    @Inject
    OrderRepository repo;

    @Inject
    OrdersPipeline pipeline;

    @Inject
    Validator validator;

    @POST
    public Response create(OrderRequest request) {
        // Manual validation
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Request body is required"))
                    .build();
        }

        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", errors))
                    .build();
        }

        // 1. Save order (transactional)
        OrderEntity e = orderService.place(request);
        String id = String.valueOf(e.getId());

        // 2. Fire-and-forget messaging (outside transaction)
        try {
            if (orderService.isDrink(request.item())) {
                pipeline.publishToBarista(new OrdersPipeline.OrderCommand("BARISTA", e.getId(), request.item(), request.quantity()));
            } else {
                pipeline.publishToKitchen(new OrdersPipeline.OrderCommand("KITCHEN", e.getId(), request.item(), request.quantity()));
            }
        } catch (Exception ex) {
            System.err.println("[orders] messaging failed (non-fatal): " + ex.getMessage());
        }

        return Response.accepted(Map.of("id", id)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") long id) {
        OrderEntity entity = repo.find(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(entity).build();
    }
}
