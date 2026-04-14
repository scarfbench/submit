package com.example.order.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.order.entity.CustomerOrder;
import com.example.order.entity.LineItem;
import com.example.order.entity.Part;
import com.example.order.entity.VendorPart;
import com.example.order.service.OrderService;

@Controller
public class OrderController {

    private static final Logger logger = Logger.getLogger("order.controller.OrderController");

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ---- Thymeleaf UI Endpoints ----

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("orders", orderService.getOrders());
        return "order";
    }

    @GetMapping("/lineItems")
    public String lineItems(@RequestParam("orderId") int orderId, Model model) {
        model.addAttribute("currentOrder", orderId);
        model.addAttribute("lineItems", orderService.getLineItems(orderId));
        model.addAttribute("parts", orderService.getAllParts());
        return "lineItem";
    }

    @PostMapping("/createOrder")
    public String createOrder(
            @RequestParam("orderId") Integer orderId,
            @RequestParam("shipmentInfo") String shipmentInfo,
            @RequestParam("status") char status,
            @RequestParam("discount") int discount) {
        orderService.createOrder(orderId, status, discount, shipmentInfo);
        return "redirect:/";
    }

    @PostMapping("/deleteOrder")
    public String deleteOrder(@RequestParam("orderId") Integer orderId) {
        orderService.removeOrder(orderId);
        return "redirect:/";
    }

    @PostMapping("/addLineItem")
    public String addLineItem(
            @RequestParam("orderId") Integer orderId,
            @RequestParam("partNumber") String partNumber,
            @RequestParam("revision") int revision) {
        orderService.addLineItem(orderId, partNumber, revision, 1);
        return "redirect:/lineItems?orderId=" + orderId;
    }

    @PostMapping("/findVendor")
    public String findVendor(@RequestParam("vendorName") String vendorName, Model model) {
        List<String> vendorResults = orderService.locateVendorsByPartialName(vendorName);
        model.addAttribute("orders", orderService.getOrders());
        model.addAttribute("vendorSearchResults", vendorResults);
        model.addAttribute("vendorName", vendorName);
        return "order";
    }

    // ---- REST API Endpoints ----

    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<List<CustomerOrder>> getOrders() {
        return ResponseEntity.ok(orderService.getOrders());
    }

    @GetMapping("/api/orders/price")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderPrice(@RequestParam("orderId") Integer orderId) {
        double price = orderService.getOrderPrice(orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("price", price);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/parts")
    @ResponseBody
    public ResponseEntity<List<Part>> getParts() {
        return ResponseEntity.ok(orderService.getAllParts());
    }

    @GetMapping("/api/vendorparts")
    @ResponseBody
    public ResponseEntity<List<VendorPart>> getVendorParts() {
        return ResponseEntity.ok(orderService.getAllVendorParts());
    }

    @GetMapping("/api/vendorparts/avgprice")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAvgPrice() {
        Map<String, Object> result = new HashMap<>();
        result.put("averagePrice", orderService.getAvgPrice());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/vendors")
    @ResponseBody
    public ResponseEntity<?> getVendors() {
        return ResponseEntity.ok(orderService.getAllVendors());
    }

    @GetMapping("/api/vendors/search")
    @ResponseBody
    public ResponseEntity<List<String>> searchVendors(@RequestParam("name") String name) {
        return ResponseEntity.ok(orderService.locateVendorsByPartialName(name));
    }

    @GetMapping("/api/vendors/totalprice")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTotalPricePerVendor(
            @RequestParam("vendorId") int vendorId) {
        Map<String, Object> result = new HashMap<>();
        result.put("vendorId", vendorId);
        result.put("totalPrice", orderService.getTotalPricePerVendor(vendorId));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/orders/lineitems")
    @ResponseBody
    public ResponseEntity<List<LineItem>> getLineItemsApi(@RequestParam("orderId") int orderId) {
        return ResponseEntity.ok(orderService.getLineItems(orderId));
    }

    @GetMapping("/api/lineitems/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> countAllItems() {
        Map<String, Object> result = new HashMap<>();
        result.put("count", orderService.countAllItems());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/orders/vendors")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reportVendorsByOrder(
            @RequestParam("orderId") Integer orderId) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("report", orderService.reportVendorsByOrder(orderId));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/bom/price")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBomPrice(
            @RequestParam("bomPartNumber") String bomPartNumber,
            @RequestParam("bomRevision") int bomRevision,
            @RequestParam("partNumber") String partNumber,
            @RequestParam("revision") int revision) {
        double price = orderService.getBillOfMaterialPrice(bomPartNumber, bomRevision, partNumber, revision);
        Map<String, Object> result = new HashMap<>();
        result.put("price", price);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/orders/adjustDiscount")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adjustDiscount(@RequestParam("adjustment") int adjustment) {
        orderService.adjustOrderDiscount(adjustment);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("adjustment", adjustment);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        return ResponseEntity.ok(result);
    }
}
