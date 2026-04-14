package com.example.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.order.entity.LineItem;
import com.example.order.entity.LineItemKey;

@Repository
public interface LineItemRepository extends JpaRepository<LineItem, LineItemKey> {

    @Query("SELECT l FROM LineItem l WHERE l.customerOrder.orderId = :orderId ORDER BY l.itemId")
    List<LineItem> findByOrderId(@Param("orderId") int orderId);
}
