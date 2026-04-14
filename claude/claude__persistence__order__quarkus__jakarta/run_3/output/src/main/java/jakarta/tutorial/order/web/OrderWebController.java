package jakarta.tutorial.order.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import jakarta.tutorial.order.entity.CustomerOrder;
import jakarta.tutorial.order.entity.LineItem;
import jakarta.tutorial.order.entity.Part;

@Path("/")
public class OrderWebController {

    @Inject
    OrderController orderController;

    @GET
    @Path("/orders")
    @Produces(MediaType.TEXT_HTML)
    public String getOrders(@QueryParam("vendorName") String vendorName,
                            @QueryParam("vendorSearchResults") List<String> vendorSearchResults) {
        List<CustomerOrder> orders = orderController.getOrders();
        boolean showVendorTable = vendorSearchResults != null && !vendorSearchResults.isEmpty();
        return buildOrdersPage(orders, vendorName, vendorSearchResults, showVendorTable);
    }

    @POST
    @Path("/submitOrder")
    @Produces(MediaType.TEXT_HTML)
    public String submitOrder(
            @FormParam("newOrderId") Integer newOrderId,
            @FormParam("newOrderStatus") String newOrderStatus,
            @FormParam("newOrderDiscount") int newOrderDiscount,
            @FormParam("newOrderShippingInfo") String newOrderShippingInfo) {
        orderController.submitOrder(newOrderId, newOrderStatus.charAt(0), newOrderDiscount, newOrderShippingInfo);
        List<CustomerOrder> orders = orderController.getOrders();
        return buildOrdersPage(orders, null, null, false);
    }

    @POST
    @Path("/removeOrder")
    @Produces(MediaType.TEXT_HTML)
    public String removeOrder(@FormParam("orderId") Integer orderId) {
        orderController.removeOrder(orderId);
        List<CustomerOrder> orders = orderController.getOrders();
        return buildOrdersPage(orders, null, null, false);
    }

    @GET
    @Path("/lineItems")
    @Produces(MediaType.TEXT_HTML)
    public String getLineItems(@QueryParam("orderId") Integer orderId) {
        List<LineItem> lineItems = orderController.getLineItems(orderId);
        List<Part> newOrderParts = orderController.getNewOrderParts();
        return buildLineItemsPage(orderId, lineItems, newOrderParts);
    }

    @POST
    @Path("/addLineItem")
    @Produces(MediaType.TEXT_HTML)
    public String addLineItem(
            @FormParam("currentOrder") Integer currentOrder,
            @FormParam("selectedPartNumber") String selectedPartNumber,
            @FormParam("selectedPartRevision") int selectedPartRevision) {
        orderController.addLineItem(currentOrder, selectedPartNumber, selectedPartRevision);
        List<LineItem> lineItems = orderController.getLineItems(currentOrder);
        List<Part> newOrderParts = orderController.getNewOrderParts();
        return buildLineItemsPage(currentOrder, lineItems, newOrderParts);
    }

    @POST
    @Path("/findVendor")
    @Produces(MediaType.TEXT_HTML)
    public String findVendor(@FormParam("vendorName") String vendorName) {
        List<String> vendorSearchResults = orderController.findVendor(vendorName);
        List<CustomerOrder> orders = orderController.getOrders();
        return buildOrdersPage(orders, vendorName, vendorSearchResults, true);
    }

    // --- HTML Builder Methods ---

    private String buildOrdersPage(List<CustomerOrder> orders, String vendorName,
                                    List<String> vendorSearchResults, boolean showVendorTable) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("    <title>Order Java Persistence Example</title>\n");
        sb.append("    <link rel=\"stylesheet\" href=\"/css/default.css\"/>\n");
        sb.append("</head>\n<body>\n");
        sb.append("    <h1>Order Java Persistence Example</h1>\n\n");

        // Orders Table
        sb.append("    <h2>View All Orders</h2>\n");
        sb.append("    <table rules=\"all\" cellpadding=\"5\">\n");
        sb.append("        <tr>\n");
        sb.append("            <th>Order ID</th>\n");
        sb.append("            <th>Shipment Info</th>\n");
        sb.append("            <th>Status</th>\n");
        sb.append("            <th>Last Updated</th>\n");
        sb.append("            <th>Discount</th>\n");
        sb.append("            <th>Actions</th>\n");
        sb.append("        </tr>\n");
        if (orders != null) {
            for (CustomerOrder order : orders) {
                sb.append("        <tr>\n");
                sb.append("            <td><a href=\"/lineItems?orderId=").append(order.getOrderId()).append("\">")
                  .append(order.getOrderId()).append("</a></td>\n");
                sb.append("            <td>").append(escapeHtml(order.getShipmentInfo())).append("</td>\n");
                sb.append("            <td>").append(order.getStatus()).append("</td>\n");
                sb.append("            <td>").append(order.getLastUpdate()).append("</td>\n");
                sb.append("            <td>").append(order.getDiscount()).append("%</td>\n");
                sb.append("            <td>\n");
                sb.append("                <form method=\"post\" action=\"/removeOrder\">\n");
                sb.append("                    <input type=\"hidden\" name=\"orderId\" value=\"").append(order.getOrderId()).append("\"/>\n");
                sb.append("                    <button type=\"submit\">Delete</button>\n");
                sb.append("                </form>\n");
                sb.append("            </td>\n");
                sb.append("        </tr>\n");
            }
        }
        sb.append("    </table>\n\n");

        // New Order Form
        sb.append("    <h2>Create New Order</h2>\n");
        sb.append("    <form method=\"post\" action=\"/submitOrder\">\n");
        sb.append("        <label for=\"orderIdInputText\">Order ID: </label>\n");
        sb.append("        <input type=\"number\" id=\"orderIdInputText\" name=\"newOrderId\" required/><br/>\n");
        sb.append("        <label for=\"shipmentInfoInputText\">Shipment Info: </label>\n");
        sb.append("        <input type=\"text\" id=\"shipmentInfoInputText\" name=\"newOrderShippingInfo\" required/><br/>\n");
        sb.append("        <label for=\"statusMenu\">Status: </label>\n");
        sb.append("        <select id=\"statusMenu\" name=\"newOrderStatus\" required>\n");
        sb.append("            <option value=\"Y\">Complete</option>\n");
        sb.append("            <option value=\"N\">Pending</option>\n");
        sb.append("        </select><br/>\n");
        sb.append("        <label for=\"discountMenu\">Discount: </label>\n");
        sb.append("        <select id=\"discountMenu\" name=\"newOrderDiscount\" required>\n");
        for (int d = 0; d <= 40; d += 5) {
            sb.append("            <option value=\"").append(d).append("\">").append(d).append(" %</option>\n");
        }
        sb.append("        </select><br/>\n");
        sb.append("        <button type=\"submit\">Submit</button>\n");
        sb.append("    </form>\n\n");

        // Find Vendor Form
        sb.append("    <h2>Find Vendors</h2>\n");
        sb.append("    <form method=\"post\" action=\"/findVendor\">\n");
        sb.append("        <label for=\"findVendorInputText\">Search for Vendors: </label>\n");
        sb.append("        <input type=\"text\" id=\"findVendorInputText\" name=\"vendorName\" value=\"")
          .append(vendorName != null ? escapeHtml(vendorName) : "").append("\" required/>\n");
        sb.append("        <button type=\"submit\" id=\"findVendorButton\">Find Vendor</button>\n");
        sb.append("    </form>\n\n");

        // Vendor Search Results
        if (showVendorTable && vendorSearchResults != null) {
            sb.append("    <table rules=\"all\" cellpadding=\"5\">\n");
            sb.append("        <tr><th>Vendor</th></tr>\n");
            for (String vendor : vendorSearchResults) {
                sb.append("        <tr><td>").append(escapeHtml(vendor)).append("</td></tr>\n");
            }
            sb.append("    </table>\n");
        }

        sb.append("</body>\n</html>");
        return sb.toString();
    }

    private String buildLineItemsPage(Integer currentOrder, List<LineItem> lineItems, List<Part> newOrderParts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("    <title>Line Items for Order ").append(currentOrder).append("</title>\n");
        sb.append("    <link rel=\"stylesheet\" href=\"/css/default.css\"/>\n");
        sb.append("</head>\n<body>\n");
        sb.append("    <h1>Line Items for Order ").append(currentOrder).append("</h1>\n\n");

        // Line Items Table
        sb.append("    <form method=\"post\">\n");
        sb.append("        <table rules=\"all\" cellpadding=\"5\">\n");
        sb.append("            <tr>\n");
        sb.append("                <th>Item ID</th>\n");
        sb.append("                <th>Quantity</th>\n");
        sb.append("                <th>Vendor Part Number</th>\n");
        sb.append("            </tr>\n");
        if (lineItems != null) {
            for (LineItem lineItem : lineItems) {
                sb.append("            <tr>\n");
                sb.append("                <td>").append(lineItem.getItemId()).append("</td>\n");
                sb.append("                <td>").append(lineItem.getQuantity()).append("</td>\n");
                String vpn = lineItem.getVendorPart() != null && lineItem.getVendorPart().getVendorPartNumber() != null
                        ? lineItem.getVendorPart().getVendorPartNumber().toString() : "N/A";
                sb.append("                <td>").append(vpn).append("</td>\n");
                sb.append("            </tr>\n");
            }
        }
        sb.append("        </table>\n");
        sb.append("        <br/>\n");

        // Available Parts Table
        sb.append("        <table id=\"orderPartsTable\" rules=\"all\" cellpadding=\"5\">\n");
        sb.append("            <tr>\n");
        sb.append("                <th>Part Number</th>\n");
        sb.append("                <th>Revision</th>\n");
        sb.append("                <th>Vendor Part Number</th>\n");
        sb.append("                <th>Add To Order</th>\n");
        sb.append("            </tr>\n");
        if (newOrderParts != null) {
            for (Part part : newOrderParts) {
                sb.append("            <tr>\n");
                sb.append("                <td>").append(escapeHtml(part.getPartNumber())).append("</td>\n");
                sb.append("                <td>").append(part.getRevision()).append("</td>\n");
                String vpn2 = part.getVendorPart() != null && part.getVendorPart().getVendorPartNumber() != null
                        ? part.getVendorPart().getVendorPartNumber().toString() : "N/A";
                sb.append("                <td>").append(vpn2).append("</td>\n");
                sb.append("                <td>\n");
                sb.append("                    <form method=\"post\" action=\"/addLineItem\">\n");
                sb.append("                        <input type=\"hidden\" name=\"currentOrder\" value=\"").append(currentOrder).append("\"/>\n");
                sb.append("                        <input type=\"hidden\" name=\"selectedPartNumber\" value=\"").append(escapeHtml(part.getPartNumber())).append("\"/>\n");
                sb.append("                        <input type=\"hidden\" name=\"selectedPartRevision\" value=\"").append(part.getRevision()).append("\"/>\n");
                sb.append("                        <button type=\"submit\">Add</button>\n");
                sb.append("                    </form>\n");
                sb.append("                </td>\n");
                sb.append("            </tr>\n");
            }
        }
        sb.append("        </table>\n");
        sb.append("        <br/>\n");
        sb.append("        <a href=\"/orders\">Back to Orders</a>\n");
        sb.append("    </form>\n");

        sb.append("</body>\n</html>");
        return sb.toString();
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
