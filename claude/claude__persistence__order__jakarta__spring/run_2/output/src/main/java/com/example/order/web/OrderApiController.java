package com.example.order.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.order.entity.CustomerOrder;
import com.example.order.entity.LineItem;
import com.example.order.entity.Part;
import com.example.order.service.OrderService;

@RestController
@RequestMapping("/api")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        return result;
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> getOrders() {
        return orderService.getOrders().stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", order.getOrderId());
            map.put("status", String.valueOf(order.getStatus()));
            map.put("discount", order.getDiscount());
            map.put("shipmentInfo", order.getShipmentInfo());
            map.put("lastUpdate", order.getLastUpdate());
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/orders/{orderId}/lineItems")
    public List<Map<String, Object>> getLineItems(@PathVariable int orderId) {
        return orderService.getLineItems(orderId).stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("itemId", item.getItemId());
            map.put("quantity", item.getQuantity());
            if (item.getVendorPart() != null) {
                map.put("vendorPartNumber", item.getVendorPart().getVendorPartNumber());
            }
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/parts")
    public List<Map<String, Object>> getParts() {
        return orderService.getAllParts().stream().map(part -> {
            Map<String, Object> map = new HashMap<>();
            map.put("partNumber", part.getPartNumber());
            map.put("revision", part.getRevision());
            map.put("description", part.getDescription());
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/vendors/search")
    public List<String> searchVendors(@RequestParam("name") String name) {
        return orderService.locateVendorsByPartialName(name);
    }

    @GetMapping("/orders/{orderId}/price")
    public Map<String, Object> getOrderPrice(@PathVariable Integer orderId) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("price", orderService.getOrderPrice(orderId));
        return result;
    }

    @GetMapping("/vendorparts/avgprice")
    public Map<String, Object> getAvgPrice() {
        Map<String, Object> result = new HashMap<>();
        result.put("averagePrice", orderService.getAvgPrice());
        return result;
    }
}
