package com.example.order.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.order.entity.CustomerOrder;
import com.example.order.entity.LineItem;
import com.example.order.entity.Part;
import com.example.order.service.OrderService;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ---- Orders ----

    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrders() {
        List<CustomerOrder> orders = orderService.getOrders();
        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", order.getOrderId());
            map.put("status", String.valueOf(order.getStatus()));
            map.put("discount", order.getDiscount());
            map.put("shipmentInfo", order.getShipmentInfo());
            map.put("lastUpdate", order.getLastUpdate());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> body) {
        Integer orderId = (Integer) body.get("orderId");
        String statusStr = (String) body.get("status");
        char status = (statusStr != null && !statusStr.isEmpty()) ? statusStr.charAt(0) : 'N';
        int discount = body.get("discount") != null ? (Integer) body.get("discount") : 0;
        String shipmentInfo = (String) body.get("shipmentInfo");

        orderService.createOrder(orderId, status, discount, shipmentInfo);
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("message", "Order created successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Map<String, String>> deleteOrder(@PathVariable Integer orderId) {
        orderService.removeOrder(orderId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Order " + orderId + " deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{orderId}/price")
    public ResponseEntity<Map<String, Object>> getOrderPrice(@PathVariable Integer orderId) {
        double price = orderService.getOrderPrice(orderId);
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("price", price);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{orderId}/vendors")
    public ResponseEntity<Map<String, Object>> getVendorsByOrder(@PathVariable Integer orderId) {
        String report = orderService.reportVendorsByOrder(orderId);
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("vendorReport", report);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders/adjust-discount")
    public ResponseEntity<Map<String, String>> adjustDiscount(@RequestBody Map<String, Integer> body) {
        int adjustment = body.getOrDefault("adjustment", 0);
        orderService.adjustOrderDiscount(adjustment);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Discounts adjusted by " + adjustment);
        return ResponseEntity.ok(response);
    }

    // ---- Line Items ----

    @GetMapping("/orders/{orderId}/lineitems")
    public ResponseEntity<List<Map<String, Object>>> getLineItems(@PathVariable int orderId) {
        List<LineItem> items = orderService.getLineItems(orderId);
        List<Map<String, Object>> result = items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("itemId", item.getItemId());
            map.put("quantity", item.getQuantity());
            if (item.getVendorPart() != null) {
                map.put("vendorPartNumber", item.getVendorPart().getVendorPartNumber());
            }
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/orders/{orderId}/lineitems")
    public ResponseEntity<Map<String, String>> addLineItem(
            @PathVariable Integer orderId,
            @RequestBody Map<String, Object> body) {
        String partNumber = (String) body.get("partNumber");
        int revision = (Integer) body.get("revision");
        int quantity = body.get("quantity") != null ? (Integer) body.get("quantity") : 1;

        orderService.addLineItem(orderId, partNumber, revision, quantity);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Line item added to order " + orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lineitems/count")
    public ResponseEntity<Map<String, Integer>> countLineItems() {
        int count = orderService.countAllItems();
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    // ---- Parts ----

    @GetMapping("/parts")
    public ResponseEntity<List<Map<String, Object>>> getParts() {
        List<Part> parts = orderService.getAllParts();
        List<Map<String, Object>> result = parts.stream().map(part -> {
            Map<String, Object> map = new HashMap<>();
            map.put("partNumber", part.getPartNumber());
            map.put("revision", part.getRevision());
            map.put("description", part.getDescription());
            if (part.getVendorPart() != null) {
                map.put("vendorPartNumber", part.getVendorPart().getVendorPartNumber());
            }
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ---- Vendors ----

    @GetMapping("/vendors/search")
    public ResponseEntity<List<String>> searchVendors(@RequestParam String name) {
        List<String> vendors = orderService.locateVendorsByPartialName(name);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/vendors/average-price")
    public ResponseEntity<Map<String, Double>> getAveragePrice() {
        Double avg = orderService.getAvgPrice();
        Map<String, Double> response = new HashMap<>();
        response.put("averagePrice", avg);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vendors/{vendorId}/total-price")
    public ResponseEntity<Map<String, Object>> getTotalPricePerVendor(@PathVariable int vendorId) {
        Double total = orderService.getTotalPricePerVendor(vendorId);
        Map<String, Object> response = new HashMap<>();
        response.put("vendorId", vendorId);
        response.put("totalPrice", total);
        return ResponseEntity.ok(response);
    }

    // ---- BOM ----

    @GetMapping("/bom/price")
    public ResponseEntity<Map<String, Object>> getBomPrice(
            @RequestParam String bomPartNumber, @RequestParam int bomRevision) {
        double price = orderService.getBillOfMaterialPrice(bomPartNumber, bomRevision);
        Map<String, Object> response = new HashMap<>();
        response.put("bomPartNumber", bomPartNumber);
        response.put("bomRevision", bomRevision);
        response.put("price", price);
        return ResponseEntity.ok(response);
    }
}
