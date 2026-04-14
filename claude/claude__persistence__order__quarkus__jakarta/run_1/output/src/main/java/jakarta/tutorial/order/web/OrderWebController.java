package jakarta.tutorial.order.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.tutorial.order.entity.CustomerOrder;
import jakarta.tutorial.order.entity.LineItem;
import jakarta.tutorial.order.entity.Part;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/")
public class OrderWebController {

    @Inject
    OrderController orderController;

    @Inject
    ThymeleafEngine thymeleaf;

    @GET
    @Path("/orders")
    @Produces(MediaType.TEXT_HTML)
    public String getOrders(@QueryParam("vendorName") String vendorName,
                            @QueryParam("vendorSearchResults") List<String> vendorSearchResults) {
        Map<String, Object> data = new HashMap<>();
        data.put("orders", toOrderMaps(orderController.getOrders()));
        data.put("vendorName", vendorName);
        data.put("vendorSearchResults", vendorSearchResults);
        data.put("findVendorTableDisabled", vendorSearchResults != null && !vendorSearchResults.isEmpty());
        return thymeleaf.render("orders", data);
    }

    @POST
    @Path("/submitOrder")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public String submitOrder(
            @FormParam("newOrderId") Integer newOrderId,
            @FormParam("newOrderStatus") String newOrderStatus,
            @FormParam("newOrderDiscount") int newOrderDiscount,
            @FormParam("newOrderShippingInfo") String newOrderShippingInfo) {
        orderController.submitOrder(newOrderId, newOrderStatus.charAt(0), newOrderDiscount, newOrderShippingInfo);
        Map<String, Object> data = new HashMap<>();
        data.put("orders", toOrderMaps(orderController.getOrders()));
        data.put("vendorName", null);
        data.put("vendorSearchResults", null);
        data.put("findVendorTableDisabled", false);
        return thymeleaf.render("orders", data);
    }

    @POST
    @Path("/removeOrder")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public String removeOrder(@FormParam("orderId") Integer orderId) {
        orderController.removeOrder(orderId);
        Map<String, Object> data = new HashMap<>();
        data.put("orders", toOrderMaps(orderController.getOrders()));
        data.put("vendorName", null);
        data.put("vendorSearchResults", null);
        data.put("findVendorTableDisabled", false);
        return thymeleaf.render("orders", data);
    }

    @GET
    @Path("/lineItems")
    @Produces(MediaType.TEXT_HTML)
    public String getLineItems(@QueryParam("orderId") Integer orderId) {
        Map<String, Object> data = new HashMap<>();
        data.put("currentOrder", orderId);
        data.put("lineItems", toLineItemMaps(orderController.getLineItems(orderId)));
        data.put("newOrderParts", toPartMaps(orderController.getNewOrderParts()));
        return thymeleaf.render("lineItems", data);
    }

    @POST
    @Path("/addLineItem")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public String addLineItem(
            @FormParam("currentOrder") Integer currentOrder,
            @FormParam("selectedPartNumber") String selectedPartNumber,
            @FormParam("selectedPartRevision") int selectedPartRevision) {
        orderController.addLineItem(currentOrder, selectedPartNumber, selectedPartRevision);
        Map<String, Object> data = new HashMap<>();
        data.put("currentOrder", currentOrder);
        data.put("lineItems", toLineItemMaps(orderController.getLineItems(currentOrder)));
        data.put("newOrderParts", toPartMaps(orderController.getNewOrderParts()));
        return thymeleaf.render("lineItems", data);
    }

    @POST
    @Path("/findVendor")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public String findVendor(@FormParam("vendorName") String vendorName) {
        Map<String, Object> data = new HashMap<>();
        data.put("orders", toOrderMaps(orderController.getOrders()));
        data.put("vendorName", vendorName);
        data.put("vendorSearchResults", orderController.findVendor(vendorName));
        data.put("findVendorTableDisabled", true);
        return thymeleaf.render("orders", data);
    }

    // Convert entities to Maps for Thymeleaf (avoids OGNL restricted access issues)
    private List<Map<String, Object>> toOrderMaps(List<CustomerOrder> orders) {
        if (orders == null) return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (CustomerOrder o : orders) {
            Map<String, Object> m = new HashMap<>();
            m.put("orderId", o.getOrderId());
            m.put("shipmentInfo", o.getShipmentInfo());
            m.put("status", String.valueOf(o.getStatus()));
            m.put("lastUpdate", o.getLastUpdate());
            m.put("discount", o.getDiscount());
            result.add(m);
        }
        return result;
    }

    private List<Map<String, Object>> toLineItemMaps(List<LineItem> lineItems) {
        if (lineItems == null) return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (LineItem li : lineItems) {
            Map<String, Object> m = new HashMap<>();
            m.put("itemId", li.getItemId());
            m.put("quantity", li.getQuantity());
            if (li.getVendorPart() != null) {
                m.put("vendorPartNumber", li.getVendorPart().getVendorPartNumber());
            } else {
                m.put("vendorPartNumber", "N/A");
            }
            result.add(m);
        }
        return result;
    }

    private List<Map<String, Object>> toPartMaps(List<Part> parts) {
        if (parts == null) return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Part p : parts) {
            Map<String, Object> m = new HashMap<>();
            m.put("partNumber", p.getPartNumber());
            m.put("revision", p.getRevision());
            if (p.getVendorPart() != null) {
                m.put("vendorPartNumber", p.getVendorPart().getVendorPartNumber());
            } else {
                m.put("vendorPartNumber", "N/A");
            }
            result.add(m);
        }
        return result;
    }
}
