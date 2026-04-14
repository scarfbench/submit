package com.example.orderspring.service;

import com.example.orderspring.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@Transactional
public class OrderService {

    @PersistenceContext(unitName = "orderPU")
    private EntityManager em;

    private static final Logger logger = Logger.getLogger("order.service.OrderService");

    public void createPart(String partNumber, int revision, String description,
                           java.util.Date revisionDate, String specification, Serializable drawing) {
        try {
            Part part = new Part(partNumber, revision, description, revisionDate, specification, drawing);
            logger.log(Level.INFO, "Created part {0}-{1}", new Object[]{partNumber, revision});
            em.persist(part);
            logger.log(Level.INFO, "Persisted part {0}-{1}", new Object[]{partNumber, revision});
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error creating part", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<Part> getAllParts() {
        TypedQuery<Part> q = em.createNamedQuery("findAllParts", Part.class);
        return q.getResultList();
    }

    public void addPartToBillOfMaterial(String bomPartNumber, int bomRevision,
                                        String partNumber, int revision) {
        logger.log(Level.INFO, "BOM part number: {0}", bomPartNumber);
        logger.log(Level.INFO, "BOM revision: {0}", bomRevision);
        logger.log(Level.INFO, "Part number: {0}", partNumber);
        logger.log(Level.INFO, "Part revision: {0}", revision);
        try {
            Part bom = findPartByNumberAndRevision(bomPartNumber, bomRevision);
            if (bom != null) {
                logger.log(Level.INFO, "BOM Part found: {0}", bom.getPartNumber());
                Part part = findPartByNumberAndRevision(partNumber, revision);
                if (part != null) {
                    logger.log(Level.INFO, "Part found: {0}", part.getPartNumber());
                    bom.getParts().add(part);
                    part.setBomPart(bom);
                    em.merge(bom);
                    em.merge(part);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error adding part to BOM", e);
        }
    }

    public void createVendor(int vendorId, String name, String address, String contact, String phone) {
        try {
            Vendor vendor = new Vendor(vendorId, name, address, contact, phone);
            em.persist(vendor);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating vendor", e);
            throw new RuntimeException(e);
        }
    }

    public void createVendorPart(String partNumber, int revision, String description,
                                 double price, int vendorId) {
        try {
            Part part = findPartByNumberAndRevision(partNumber, revision);
            if (part != null) {
                VendorPart vendorPart = new VendorPart(description, price, part);
                em.persist(vendorPart);

                Vendor vendor = em.find(Vendor.class, vendorId);
                if (vendor != null) {
                    vendor.addVendorPart(vendorPart);
                    vendorPart.setVendor(vendor);
                    em.merge(vendor);
                    em.merge(vendorPart);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating vendor part", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public void createOrder(Integer orderId, char status, int discount, String shipmentInfo) {
        if (orderId == null) throw new IllegalArgumentException("Order ID is required");
        CustomerOrder existing = em.find(CustomerOrder.class, orderId);
        if (existing != null) {
            throw new IllegalStateException("Order ID " + orderId + " already exists");
        }
        em.persist(new CustomerOrder(orderId, status, discount, shipmentInfo));
    }

    public List<CustomerOrder> getOrders() {
        try {
            TypedQuery<CustomerOrder> q = em.createNamedQuery("findAllOrders", CustomerOrder.class);
            return q.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting orders", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public void addLineItem(Integer orderId, String partNumber, int revision, int quantity) {
        try {
            CustomerOrder order = em.find(CustomerOrder.class, orderId);
            if (order != null) {
                logger.log(Level.INFO, "Found order ID {0}", orderId);
                Part part = findPartByNumberAndRevision(partNumber, revision);
                if (part != null && part.getVendorPart() != null) {
                    LineItem lineItem = new LineItem(order, quantity, part.getVendorPart());
                    order.addLineItem(lineItem);
                    em.persist(lineItem);
                    em.merge(order);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Couldn't add {0} to order ID {1}.", new Object[]{partNumber, orderId});
            throw new RuntimeException(e.getMessage());
        }
    }

    public double getBillOfMaterialPrice(String bomPartNumber, int bomRevision,
                                         String partNumber, int revision) {
        double price = 0.0;
        try {
            Part bom = findPartByNumberAndRevision(bomPartNumber, bomRevision);
            if (bom != null) {
                Collection<Part> parts = bom.getParts();
                for (Part part : parts) {
                    VendorPart vendorPart = part.getVendorPart();
                    if (vendorPart != null) {
                        price += vendorPart.getPrice();
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating BOM price", e);
            throw new RuntimeException(e.getMessage());
        }
        return price;
    }

    public double getOrderPrice(Integer orderId) {
        double price = 0.0;
        try {
            CustomerOrder order = em.find(CustomerOrder.class, orderId);
            if (order != null) {
                price = order.calculateAmount();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating order price", e);
            throw new RuntimeException(e.getMessage());
        }
        return price;
    }

    public void adjustOrderDiscount(int adjustment) {
        try {
            List<CustomerOrder> orders = getOrders();
            for (CustomerOrder order : orders) {
                int newDiscount = order.getDiscount() + adjustment;
                order.setDiscount((newDiscount > 0) ? newDiscount : 0);
                em.merge(order);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error adjusting order discount", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public Double getAvgPrice() {
        try {
            TypedQuery<Double> q = em.createNamedQuery("findAverageVendorPartPrice", Double.class);
            return q.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting average price", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public Double getTotalPricePerVendor(int vendorId) {
        try {
            TypedQuery<Double> q = em.createNamedQuery("findTotalVendorPartPricePerVendor", Double.class);
            q.setParameter("id", vendorId);
            return q.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total price per vendor", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<String> locateVendorsByPartialName(String name) {
        List<String> names = new ArrayList<>();
        try {
            List<Vendor> vendors = findVendorsByName(name);
            for (Vendor vendor : vendors) {
                names.add(vendor.getName());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error locating vendors", e);
            throw new RuntimeException(e.getMessage());
        }
        return names;
    }

    public int countAllItems() {
        int count = 0;
        try {
            TypedQuery<LineItem> q = em.createNamedQuery("findAllLineItems", LineItem.class);
            count = q.getResultList().size();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error counting items", e);
            throw new RuntimeException(e.getMessage());
        }
        return count;
    }

    public List<LineItem> getLineItems(int orderId) {
        try {
            TypedQuery<LineItem> q = em.createNamedQuery("findLineItemsByOrderId", LineItem.class);
            q.setParameter("orderId", orderId);
            return q.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting line items", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public void removeOrder(Integer orderId) {
        // Delete line items first
        TypedQuery<LineItem> q = em.createNamedQuery("findLineItemsByOrderId", LineItem.class);
        q.setParameter("orderId", orderId);
        List<LineItem> items = q.getResultList();
        for (LineItem item : items) {
            em.remove(em.merge(item));
        }
        CustomerOrder order = em.find(CustomerOrder.class, orderId);
        if (order != null) {
            order.getLineItems().clear();
            em.remove(order);
        }
    }

    public String reportVendorsByOrder(Integer orderId) {
        StringBuilder report = new StringBuilder();
        try {
            TypedQuery<Vendor> q = em.createNamedQuery("findVendorByCustomerOrder", Vendor.class);
            q.setParameter("id", orderId);
            List<Vendor> vendors = q.getResultList();
            for (Vendor vendor : vendors) {
                report.append(vendor.getVendorId()).append(' ')
                        .append(vendor.getName()).append(' ')
                        .append(vendor.getContact()).append('\n');
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reporting vendors by order", e);
            throw new RuntimeException(e);
        }
        return report.toString();
    }

    // Additional methods for JSF integration
    public List<CustomerOrder> getAllOrders() {
        return getOrders();
    }

    public List<LineItem> getLineItemsByOrderId(Integer orderId) {
        return getLineItems(orderId);
    }

    public List<Vendor> findVendorsByName(String name) {
        try {
            TypedQuery<Vendor> q = em.createNamedQuery("findVendorsByPartialName", Vendor.class);
            q.setParameter("name", name);
            return q.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding vendors by name", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    // Helper method replacing Spring Data repository
    private Part findPartByNumberAndRevision(String partNumber, int revision) {
        PartKey key = new PartKey(partNumber, revision);
        return em.find(Part.class, key);
    }
}
