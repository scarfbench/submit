package com.example.orderspring.repository;

import com.example.orderspring.entity.CustomerOrder;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@Stateless
public class CustomerOrderRepository {

    @PersistenceContext(unitName = "order-pu")
    private EntityManager em;

    public List<CustomerOrder> findAllOrders() {
        return em.createQuery("SELECT co FROM CustomerOrder co ORDER BY co.orderId", CustomerOrder.class)
                 .getResultList();
    }

    public Optional<CustomerOrder> findById(Integer id) {
        CustomerOrder order = em.find(CustomerOrder.class, id);
        return Optional.ofNullable(order);
    }

    public boolean existsById(Integer id) {
        return em.find(CustomerOrder.class, id) != null;
    }

    public CustomerOrder save(CustomerOrder order) {
        if (em.find(CustomerOrder.class, order.getOrderId()) == null) {
            em.persist(order);
            return order;
        } else {
            return em.merge(order);
        }
    }

    public void deleteById(Integer id) {
        CustomerOrder order = em.find(CustomerOrder.class, id);
        if (order != null) {
            em.remove(em.contains(order) ? order : em.merge(order));
        }
    }
}
