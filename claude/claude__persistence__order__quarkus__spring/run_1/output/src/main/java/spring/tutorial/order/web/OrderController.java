package spring.tutorial.order.web;

import spring.tutorial.order.entity.CustomerOrder;
import spring.tutorial.order.entity.LineItem;
import spring.tutorial.order.entity.Part;
import spring.tutorial.order.repository.CustomerOrderRepository;
import spring.tutorial.order.repository.LineItemRepository;
import spring.tutorial.order.repository.PartRepository;
import spring.tutorial.order.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

@Component
@RequestScope
public class OrderController implements Serializable {
    private static final long serialVersionUID = 2142383151318489373L;
    private static final Logger logger = Logger.getLogger(OrderController.class.getName());

    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private LineItemRepository lineItemRepository;
    @Autowired
    private PartRepository partRepository;
    @Autowired
    private VendorRepository vendorRepository;

    public List<CustomerOrder> getOrders() {
        try {
            return customerOrderRepository.getOrders();
        } catch (Exception e) {
            logger.warning("Couldn't get orders: " + e.getMessage());
            return null;
        }
    }

    public List<LineItem> getLineItems(Integer currentOrder) {
        try {
            return lineItemRepository.getLineItems(currentOrder);
        } catch (Exception e) {
            logger.warning("Couldn't get line items for order ID " + currentOrder + ": " + e.getMessage());
            return null;
        }
    }

    public String removeOrder(Integer orderId) {
        try {
            customerOrderRepository.removeOrder(orderId);
            logger.info("Removed order " + orderId);
            return "/orders";
        } catch (Exception e) {
            logger.warning("Problem removing order ID " + orderId + ": " + e.getMessage());
            return null;
        }
    }

    public List<String> findVendor(String vendorName) {
        try {
            List<String> vendorSearchResults = vendorRepository.locateVendorsByPartialName(vendorName);
            logger.info("Found " + vendorSearchResults.size() + " vendor(s) using search string " + vendorName);
            return vendorSearchResults;
        } catch (Exception e) {
            logger.warning("Problem finding vendors: " + e.getMessage());
            return null;
        }
    }

    public String submitOrder(Integer newOrderId, char newOrderStatus, int newOrderDiscount, String newOrderShippingInfo) {
        try {
            customerOrderRepository.createOrder(newOrderId, newOrderStatus, newOrderDiscount, newOrderShippingInfo);
            logger.info("Created new order with ID " + newOrderId);
            return "/orders";
        } catch (Exception e) {
            logger.warning("Problem creating order: " + e.getMessage());
            return null;
        }
    }

    public String addLineItem(Integer currentOrder, String selectedPartNumber, int selectedPartRevision) {
        try {
            lineItemRepository.addLineItem(currentOrder, selectedPartNumber, selectedPartRevision, 1);
            logger.info("Added line item to order ID " + currentOrder);
            return "/lineItems?orderId=" + currentOrder;
        } catch (Exception e) {
            logger.warning("Problem adding line item to order ID " + currentOrder + ": " + e.getMessage());
            return null;
        }
    }

    public List<Part> getNewOrderParts() {
        return partRepository.getAllParts();
    }
}
