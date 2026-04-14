package com.example.order.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order.entity.CustomerOrder;
import com.example.order.entity.LineItem;
import com.example.order.entity.Part;
import com.example.order.entity.PartKey;
import com.example.order.entity.Vendor;
import com.example.order.entity.VendorPart;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class OrderService {

    @PersistenceContext
    private EntityManager em;

    private static final Logger logger = Logger.getLogger("order.service.OrderService");

    public void createPart(String partNumber,
            int revision,
            String description,
            java.util.Date revisionDate,
            String specification,
            Serializable drawing) {
        try {
            Part part = new Part(partNumber,
                    revision,
                    description,
                    revisionDate,
                    specification,
                    drawing);
            logger.log(Level.INFO, "Created part {0}-{1}", new Object[] { partNumber, revision });
            em.persist(part);
            logger.log(Level.INFO, "Persisted part {0}-{1}", new Object[] { partNumber, revision });
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public List<Part> getAllParts() {
        @SuppressWarnings("unchecked")
        List<Part> parts = em.createQuery("SELECT p FROM Part p ORDER BY p.partNumber").getResultList();
        return parts;
    }

    public void addPartToBillOfMaterial(String bomPartNumber,
            int bomRevision,
            String partNumber,
            int revision) {
        logger.log(Level.INFO, "BOM part number: {0}", bomPartNumber);
        logger.log(Level.INFO, "BOM revision: {0}", bomRevision);
        logger.log(Level.INFO, "Part number: {0}", partNumber);
        logger.log(Level.INFO, "Part revision: {0}", revision);
        try {
            PartKey bomKey = new PartKey();
            bomKey.setPartNumber(bomPartNumber);
            bomKey.setRevision(bomRevision);

            Part bom = em.find(Part.class, bomKey);
            logger.log(Level.INFO, "BOM Part found: {0}", bom.getPartNumber());

            PartKey partKey = new PartKey();
            partKey.setPartNumber(partNumber);
            partKey.setRevision(revision);

            Part part = em.find(Part.class, partKey);
            logger.log(Level.INFO, "Part found: {0}", part.getPartNumber());
            bom.getParts().add(part);
            part.setBomPart(bom);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error adding part to BOM: {0}", e.getMessage());
        }
    }

    public void createVendor(int vendorId,
            String name,
            String address,
            String contact,
            String phone) {
        try {
            Vendor vendor = new Vendor(vendorId, name, address, contact, phone);
            em.persist(vendor);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void createVendorPart(String partNumber,
            int revision,
            String description,
            double price,
            int vendorId) {
        try {
            PartKey pkey = new PartKey();
            pkey.setPartNumber(partNumber);
            pkey.setRevision(revision);

            Part part = em.find(Part.class, pkey);

            VendorPart vendorPart = new VendorPart(description, price, part);
            em.persist(vendorPart);

            Vendor vendor = em.find(Vendor.class, vendorId);
            vendor.addVendorPart(vendorPart);
            vendorPart.setVendor(vendor);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void createOrder(Integer orderId, char status, int discount, String shipmentInfo) {
        try {
            CustomerOrder order = new CustomerOrder(orderId, status, discount, shipmentInfo);
            em.persist(order);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<CustomerOrder> getOrders() {
        try {
            @SuppressWarnings("unchecked")
            List<CustomerOrder> result = em.createQuery(
                    "SELECT co FROM CustomerOrder co ORDER BY co.orderId").getResultList();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void addLineItem(Integer orderId, String partNumber, int revision, int quantity) {
        try {
            CustomerOrder order = em.find(CustomerOrder.class, orderId);
            logger.log(Level.INFO, "Found order ID {0}", orderId);

            PartKey pkey = new PartKey();
            pkey.setPartNumber(partNumber);
            pkey.setRevision(revision);

            Part part = em.find(Part.class, pkey);

            LineItem lineItem = new LineItem(order, quantity, part.getVendorPart());
            order.addLineItem(lineItem);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Couldn''t add {0} to order ID {1}.",
                    new Object[] { partNumber, orderId });
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public double getBillOfMaterialPrice(String bomPartNumber, int bomRevision,
            String partNumber, int revision) {
        double price = 0.0;
        try {
            PartKey bomkey = new PartKey();
            bomkey.setPartNumber(bomPartNumber);
            bomkey.setRevision(bomRevision);

            Part bom = em.find(Part.class, bomkey);
            Collection<Part> parts = bom.getParts();
            for (Part part : parts) {
                VendorPart vendorPart = part.getVendorPart();
                price += vendorPart.getPrice();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return price;
    }

    public double getOrderPrice(Integer orderId) {
        double price = 0.0;
        try {
            CustomerOrder order = em.find(CustomerOrder.class, orderId);
            price = order.calculateAmmount();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return price;
    }

    public void adjustOrderDiscount(int adjustment) {
        try {
            @SuppressWarnings("unchecked")
            List<CustomerOrder> orders = em.createQuery(
                    "SELECT co FROM CustomerOrder co ORDER BY co.orderId").getResultList();
            for (CustomerOrder order : orders) {
                int newDiscount = order.getDiscount() + adjustment;
                order.setDiscount((newDiscount > 0) ? newDiscount : 0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Double getAvgPrice() {
        try {
            return (Double) em.createQuery(
                    "SELECT AVG(vp.price) FROM VendorPart vp").getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Double getTotalPricePerVendor(int vendorId) {
        try {
            return (Double) em.createQuery(
                    "SELECT SUM(vp.price) FROM VendorPart vp WHERE vp.vendor.vendorId = :id")
                    .setParameter("id", vendorId)
                    .getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<String> locateVendorsByPartialName(String name) {
        List<String> names = new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            List<Vendor> vendors = em.createQuery(
                    "SELECT v FROM Vendor v WHERE LOCATE(:name, v.name) > 0")
                    .setParameter("name", name)
                    .getResultList();
            for (Vendor vendor : vendors) {
                names.add(vendor.getName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return names;
    }

    public int countAllItems() {
        int count = 0;
        try {
            count = em.createQuery("SELECT l FROM LineItem l")
                    .getResultList()
                    .size();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    public List<LineItem> getLineItems(int orderId) {
        try {
            return em.createQuery(
                    "SELECT l FROM LineItem l WHERE l.customerOrder.orderId = :orderId ORDER BY l.itemId")
                    .setParameter("orderId", orderId)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void removeOrder(Integer orderId) {
        try {
            CustomerOrder order = em.find(CustomerOrder.class, orderId);
            em.remove(order);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String reportVendorsByOrder(Integer orderId) {
        StringBuilder report = new StringBuilder();
        try {
            @SuppressWarnings("unchecked")
            List<Vendor> vendors = em.createQuery(
                    "SELECT DISTINCT l.vendorPart.vendor " +
                            "FROM CustomerOrder co, IN(co.lineItems) l " +
                            "WHERE co.orderId = :id " +
                            "ORDER BY l.vendorPart.vendor.name")
                    .setParameter("id", orderId)
                    .getResultList();
            for (Vendor vendor : vendors) {
                report.append(vendor.getVendorId()).append(' ')
                        .append(vendor.getName()).append(' ')
                        .append(vendor.getContact()).append('\n');
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return report.toString();
    }

    public List<VendorPart> getAllVendorParts() {
        @SuppressWarnings("unchecked")
        List<VendorPart> result = em.createQuery(
                "SELECT vp FROM VendorPart vp ORDER BY vp.vendorPartNumber").getResultList();
        return result;
    }

    public List<Vendor> getAllVendors() {
        @SuppressWarnings("unchecked")
        List<Vendor> result = em.createQuery(
                "SELECT v FROM Vendor v ORDER BY v.vendorId").getResultList();
        return result;
    }
}
