package com.example.orderspring.repository;

import com.example.orderspring.entity.Vendor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VendorRepository {

    @Inject
    EntityManager em;

    public List<Vendor> findVendorsByPartialName(String name) {
        return em.createQuery("SELECT v FROM Vendor v WHERE LOCATE(:name, v.name) > 0", Vendor.class)
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

    public Optional<Vendor> findById(int id) {
        Vendor vendor = em.find(Vendor.class, id);
        return Optional.ofNullable(vendor);
    }

    public Vendor save(Vendor vendor) {
        if (em.find(Vendor.class, vendor.getVendorId()) != null) {
            return em.merge(vendor);
        } else {
            em.persist(vendor);
            return vendor;
        }
    }
}
