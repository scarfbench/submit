package com.example.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.order.entity.Vendor;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Integer> {

    @Query("SELECT v FROM Vendor v WHERE v.name LIKE %:name%")
    List<Vendor> findByPartialName(@Param("name") String name);

    @Query("SELECT DISTINCT l.vendorPart.vendor FROM CustomerOrder co JOIN co.lineItems l WHERE co.orderId = :id ORDER BY l.vendorPart.vendor.name")
    List<Vendor> findVendorsByOrderId(@Param("id") Integer orderId);
}
