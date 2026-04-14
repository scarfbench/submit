package com.example.orderspring.repository;

import com.example.orderspring.entity.VendorPart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class VendorPartRepository {

    @Inject
    EntityManager em;

    public Double findAverageVendorPartPrice() {
        return em.createQuery("SELECT AVG(vp.price) FROM VendorPart vp", Double.class)
                .getSingleResult();
    }

    public Double findTotalVendorPartPricePerVendor(int vendorId) {
        return em.createQuery("SELECT SUM(vp.price) FROM VendorPart vp WHERE vp.vendor.vendorId = :id", Double.class)
                .setParameter("id", vendorId)
                .getSingleResult();
    }

    public List<VendorPart> findAllVendorParts() {
        return em.createQuery("SELECT vp FROM VendorPart vp ORDER BY vp.vendorPartNumber", VendorPart.class)
                .getResultList();
    }

    public VendorPart save(VendorPart vendorPart) {
        if (vendorPart.getVendorPartNumber() != null && em.find(VendorPart.class, vendorPart.getVendorPartNumber()) != null) {
            return em.merge(vendorPart);
        } else {
            em.persist(vendorPart);
            return vendorPart;
        }
    }
}
