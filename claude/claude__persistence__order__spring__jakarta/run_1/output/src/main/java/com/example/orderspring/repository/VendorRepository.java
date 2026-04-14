package com.example.orderspring.repository;

import com.example.orderspring.entity.Vendor;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@Stateless
public class VendorRepository {

    @PersistenceContext(unitName = "order-pu")
    private EntityManager em;

    public Optional<Vendor> findById(int vendorId) {
        Vendor vendor = em.find(Vendor.class, vendorId);
        return Optional.ofNullable(vendor);
    }

    public List<Vendor> findVendorsByPartialName(String name) {
        return em.createQuery(
                "SELECT v FROM Vendor v WHERE LOCATE(:name, v.name) > 0", Vendor.class)
                .setParameter("name", name)
                .getResultList();
    }

    public List<Vendor> findVendorByCustomerOrder(Integer orderId) {
        return em.createQuery(
                "SELECT DISTINCT l.vendorPart.vendor FROM CustomerOrder co, IN(co.lineItems) l WHERE co.orderId = :id ORDER BY l.vendorPart.vendor.name",
                Vendor.class)
                .setParameter("id", orderId)
                .getResultList();
    }

    public Vendor save(Vendor vendor) {
        Vendor existing = em.find(Vendor.class, vendor.getVendorId());
        if (existing == null) {
            em.persist(vendor);
            return vendor;
        } else {
            return em.merge(vendor);
        }
    }
}
