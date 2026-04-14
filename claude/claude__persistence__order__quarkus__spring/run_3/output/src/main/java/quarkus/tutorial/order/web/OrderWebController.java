package quarkus.tutorial.order.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OrderWebController {

    @Autowired
    private OrderController orderController;

    @GetMapping("/orders")
    public String getOrders(@RequestParam(value = "vendorName", required = false) String vendorName,
                            Model model) {
        model.addAttribute("orders", orderController.getOrders());
        model.addAttribute("vendorName", vendorName);
        model.addAttribute("vendorSearchResults", null);
        model.addAttribute("findVendorTableDisabled", false);
        return "orders";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(
            @RequestParam("newOrderId") Integer newOrderId,
            @RequestParam("newOrderStatus") String newOrderStatus,
            @RequestParam("newOrderDiscount") int newOrderDiscount,
            @RequestParam("newOrderShippingInfo") String newOrderShippingInfo,
            Model model) {
        orderController.submitOrder(newOrderId, newOrderStatus.charAt(0), newOrderDiscount, newOrderShippingInfo);
        model.addAttribute("orders", orderController.getOrders());
        model.addAttribute("vendorName", null);
        model.addAttribute("vendorSearchResults", null);
        model.addAttribute("findVendorTableDisabled", false);
        return "orders";
    }

    @PostMapping("/removeOrder")
    public String removeOrder(@RequestParam("orderId") Integer orderId, Model model) {
        orderController.removeOrder(orderId);
        model.addAttribute("orders", orderController.getOrders());
        model.addAttribute("vendorName", null);
        model.addAttribute("vendorSearchResults", null);
        model.addAttribute("findVendorTableDisabled", false);
        return "orders";
    }

    @GetMapping("/lineItems")
    public String getLineItems(@RequestParam("orderId") Integer orderId, Model model) {
        model.addAttribute("currentOrder", orderId);
        model.addAttribute("lineItems", orderController.getLineItems(orderId));
        model.addAttribute("newOrderParts", orderController.getNewOrderParts());
        return "lineItems";
    }

    @PostMapping("/addLineItem")
    public String addLineItem(
            @RequestParam("currentOrder") Integer currentOrder,
            @RequestParam("selectedPartNumber") String selectedPartNumber,
            @RequestParam("selectedPartRevision") int selectedPartRevision,
            Model model) {
        orderController.addLineItem(currentOrder, selectedPartNumber, selectedPartRevision);
        model.addAttribute("currentOrder", currentOrder);
        model.addAttribute("lineItems", orderController.getLineItems(currentOrder));
        model.addAttribute("newOrderParts", orderController.getNewOrderParts());
        return "lineItems";
    }

    @PostMapping("/findVendor")
    public String findVendor(@RequestParam("vendorName") String vendorName, Model model) {
        model.addAttribute("orders", orderController.getOrders());
        model.addAttribute("vendorName", vendorName);
        model.addAttribute("vendorSearchResults", orderController.findVendor(vendorName));
        model.addAttribute("findVendorTableDisabled", true);
        return "orders";
    }
}
