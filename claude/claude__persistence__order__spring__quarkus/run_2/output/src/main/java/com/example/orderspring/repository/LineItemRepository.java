package com.example.orderspring.repository;

import com.example.orderspring.entity.LineItem;
import com.example.orderspring.entity.LineItemKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class LineItemRepository {

    @Inject
    EntityManager em;

    public List<LineItem> findAllLineItems() {
        return em.createQuery("SELECT l FROM LineItem l", LineItem.class)
                .getResultList();
    }

    public List<LineItem> findLineItemsByOrderId(Integer orderId) {
        return em.createQuery(
                "SELECT l FROM LineItem l WHERE l.customerOrder.orderId = :orderId ORDER BY l.itemId",
                LineItem.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public LineItem findLineItemById(int itemId, Integer orderId) {
        List<LineItem> results = em.createQuery(
                "SELECT DISTINCT l FROM LineItem l WHERE l.itemId = :itemId AND l.customerOrder.orderId = :orderId",
                LineItem.class)
                .setParameter("itemId", itemId)
                .setParameter("orderId", orderId)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public void deleteAllByOrderId(Integer orderId) {
        em.createQuery("DELETE FROM LineItem l WHERE l.customerOrder.orderId = :orderId")
                .setParameter("orderId", orderId)
                .executeUpdate();
        em.flush();
        em.clear();
    }

    public LineItem save(LineItem lineItem) {
        if (em.contains(lineItem)) {
            return em.merge(lineItem);
        }
        LineItemKey key = new LineItemKey(
            lineItem.getCustomerOrder() != null ? lineItem.getCustomerOrder().getOrderId() : null,
            lineItem.getItemId()
        );
        LineItem existing = null;
        try {
            existing = em.find(LineItem.class, key);
        } catch (Exception e) {
            // ignore
        }
        if (existing == null) {
            em.persist(lineItem);
            return lineItem;
        } else {
            return em.merge(lineItem);
        }
    }
}
