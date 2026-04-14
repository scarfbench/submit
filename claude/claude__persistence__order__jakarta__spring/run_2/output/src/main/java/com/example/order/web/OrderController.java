package com.example.order.web;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.order.entity.CustomerOrder;
import com.example.order.entity.LineItem;
import com.example.order.entity.Part;
import com.example.order.service.OrderService;

@Controller
public class OrderController {

    private static final Logger logger = Logger.getLogger("order.web.OrderController");

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping({"/", "/order"})
    public String showOrders(Model model) {
        List<CustomerOrder> orders = orderService.getOrders();
        model.addAttribute("orders", orders);
        return "order";
    }

    @PostMapping("/order/delete")
    public String deleteOrder(@RequestParam("orderId") Integer orderId) {
        try {
            orderService.removeOrder(orderId);
            logger.log(Level.INFO, "Removed order {0}.", orderId);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error removing order {0}.", orderId);
        }
        return "redirect:/order";
    }

    @PostMapping("/order/create")
    public String createOrder(
            @RequestParam("orderId") Integer orderId,
            @RequestParam("shipmentInfo") String shipmentInfo,
            @RequestParam("status") char status,
            @RequestParam("discount") int discount) {
        try {
            orderService.createOrder(orderId, status, discount, shipmentInfo);
            logger.log(Level.INFO, "Created new order with order ID {0}, status {1}, "
                    + "discount {2}, and shipping info {3}.",
                    new Object[]{orderId, status, discount, shipmentInfo});
        } catch (Exception e) {
            logger.warning("Problem creating order.");
        }
        return "redirect:/order";
    }

    @GetMapping("/lineItem")
    public String showLineItems(@RequestParam("orderId") int orderId, Model model) {
        List<LineItem> lineItems = orderService.getLineItems(orderId);
        List<Part> parts = orderService.getAllParts();
        model.addAttribute("lineItems", lineItems);
        model.addAttribute("parts", parts);
        model.addAttribute("currentOrder", orderId);
        return "lineItem";
    }

    @PostMapping("/lineItem/add")
    public String addLineItem(
            @RequestParam("orderId") int orderId,
            @RequestParam("partNumber") String partNumber,
            @RequestParam("revision") int revision) {
        try {
            orderService.addLineItem(orderId, partNumber, revision, 1);
            logger.log(Level.INFO, "Adding line item to order # {0}", orderId);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Problem adding line item to order ID {0}", orderId);
        }
        return "redirect:/lineItem?orderId=" + orderId;
    }

    @PostMapping("/vendor/find")
    public String findVendor(@RequestParam("vendorName") String vendorName, Model model) {
        List<String> vendors = orderService.locateVendorsByPartialName(vendorName);
        List<CustomerOrder> orders = orderService.getOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("vendorSearchResults", vendors);
        model.addAttribute("vendorName", vendorName);
        logger.log(Level.INFO, "Found {0} vendor(s) using the search string {1}.",
                new Object[]{vendors.size(), vendorName});
        return "order";
    }
}
