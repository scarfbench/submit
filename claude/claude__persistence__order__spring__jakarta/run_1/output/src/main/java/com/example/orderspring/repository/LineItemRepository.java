package com.example.orderspring.repository;

import com.example.orderspring.entity.LineItem;
import com.example.orderspring.entity.LineItemKey;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class LineItemRepository {

    @PersistenceContext(unitName = "order-pu")
    private EntityManager em;

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
    }

    public LineItem save(LineItem lineItem) {
        LineItem existing = findLineItemById(lineItem.getItemId(),
            lineItem.getCustomerOrder() != null ? lineItem.getCustomerOrder().getOrderId() : null);
        if (existing == null) {
            em.persist(lineItem);
            return lineItem;
        } else {
            return em.merge(lineItem);
        }
    }
}
