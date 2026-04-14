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
        Part part = new Part(partNumber,
                revision,
                description,
                revisionDate,
                specification,
                drawing);
        logger.log(Level.INFO, "Created part {0}-{1}", new Object[]{partNumber, revision});
        em.persist(part);
        logger.log(Level.INFO, "Persisted part {0}-{1}", new Object[]{partNumber, revision});
    }

    @Transactional(readOnly = true)
    public List<Part> getAllParts() {
        return em.createNamedQuery("findAllParts", Part.class).getResultList();
    }

    public void addPartToBillOfMaterial(String bomPartNumber,
            int bomRevision,
            String partNumber,
            int revision) {
        logger.log(Level.INFO, "BOM part number: {0}", bomPartNumber);
        logger.log(Level.INFO, "BOM revision: {0}", bomRevision);
        logger.log(Level.INFO, "Part number: {0}", partNumber);
        logger.log(Level.INFO, "Part revision: {0}", revision);

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
    }

    public void createVendor(int vendorId,
            String name,
            String address,
            String contact,
            String phone) {
        Vendor vendor = new Vendor(vendorId, name, address, contact, phone);
        em.persist(vendor);
    }

    public void createVendorPart(String partNumber,
            int revision,
            String description,
            double price,
            int vendorId) {
        PartKey pkey = new PartKey();
        pkey.setPartNumber(partNumber);
        pkey.setRevision(revision);

        Part part = em.find(Part.class, pkey);

        VendorPart vendorPart = new VendorPart(description, price, part);
        em.persist(vendorPart);

        Vendor vendor = em.find(Vendor.class, vendorId);
        vendor.addVendorPart(vendorPart);
        vendorPart.setVendor(vendor);
    }

    public void createOrder(Integer orderId, char status, int discount, String shipmentInfo) {
        CustomerOrder order = new CustomerOrder(orderId, status, discount, shipmentInfo);
        em.persist(order);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrders() {
        return em.createNamedQuery("findAllOrders", CustomerOrder.class).getResultList();
    }

    public void addLineItem(Integer orderId, String partNumber, int revision, int quantity) {
        CustomerOrder order = em.find(CustomerOrder.class, orderId);
        logger.log(Level.INFO, "Found order ID {0}", orderId);

        PartKey pkey = new PartKey();
        pkey.setPartNumber(partNumber);
        pkey.setRevision(revision);

        Part part = em.find(Part.class, pkey);

        LineItem lineItem = new LineItem(order, quantity, part.getVendorPart());
        order.addLineItem(lineItem);
    }

    @Transactional(readOnly = true)
    public double getBillOfMaterialPrice(String bomPartNumber, int bomRevision,
            String partNumber, int revision) {
        double price = 0.0;
        PartKey bomkey = new PartKey();
        bomkey.setPartNumber(bomPartNumber);
        bomkey.setRevision(bomRevision);

        Part bom = em.find(Part.class, bomkey);
        Collection<Part> parts = bom.getParts();
        for (Part part : parts) {
            VendorPart vendorPart = part.getVendorPart();
            price += vendorPart.getPrice();
        }
        return price;
    }

    @Transactional(readOnly = true)
    public double getOrderPrice(Integer orderId) {
        CustomerOrder order = em.find(CustomerOrder.class, orderId);
        return order.calculateAmmount();
    }

    public void adjustOrderDiscount(int adjustment) {
        List<CustomerOrder> orders = em.createNamedQuery("findAllOrders", CustomerOrder.class)
                .getResultList();
        for (CustomerOrder order : orders) {
            int newDiscount = order.getDiscount() + adjustment;
            order.setDiscount((newDiscount > 0) ? newDiscount : 0);
        }
    }

    @Transactional(readOnly = true)
    public Double getAvgPrice() {
        return (Double) em.createNamedQuery("findAverageVendorPartPrice")
                .getSingleResult();
    }

    @Transactional(readOnly = true)
    public Double getTotalPricePerVendor(int vendorId) {
        return (Double) em.createNamedQuery("findTotalVendorPartPricePerVendor")
                .setParameter("id", vendorId)
                .getSingleResult();
    }

    @Transactional(readOnly = true)
    public List<String> locateVendorsByPartialName(String name) {
        List<String> names = new ArrayList<>();
        List vendors = em.createNamedQuery("findVendorsByPartialName")
                .setParameter("name", name)
                .getResultList();
        for (Iterator iterator = vendors.iterator(); iterator.hasNext();) {
            Vendor vendor = (Vendor) iterator.next();
            names.add(vendor.getName());
        }
        return names;
    }

    @Transactional(readOnly = true)
    public int countAllItems() {
        return em.createNamedQuery("findAllLineItems")
                .getResultList()
                .size();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<LineItem> getLineItems(int orderId) {
        return em.createNamedQuery("findLineItemsByOrderId")
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public void removeOrder(Integer orderId) {
        CustomerOrder order = em.find(CustomerOrder.class, orderId);
        em.remove(order);
    }

    @Transactional(readOnly = true)
    public String reportVendorsByOrder(Integer orderId) {
        StringBuilder report = new StringBuilder();
        List vendors = em.createNamedQuery("findVendorByOrder")
                .setParameter("id", orderId)
                .getResultList();
        for (Iterator iterator = vendors.iterator(); iterator.hasNext();) {
            Vendor vendor = (Vendor) iterator.next();
            report.append(vendor.getVendorId()).append(' ')
                    .append(vendor.getName()).append(' ')
                    .append(vendor.getContact()).append('\n');
        }
        return report.toString();
    }
}
