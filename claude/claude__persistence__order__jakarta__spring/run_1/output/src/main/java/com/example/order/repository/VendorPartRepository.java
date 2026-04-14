package com.example.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.order.entity.VendorPart;

@Repository
public interface VendorPartRepository extends JpaRepository<VendorPart, Long> {

    List<VendorPart> findAllByOrderByVendorPartNumberAsc();

    @Query("SELECT AVG(vp.price) FROM VendorPart vp")
    Double findAveragePrice();

    @Query("SELECT SUM(vp.price) FROM VendorPart vp WHERE vp.vendor.vendorId = :id")
    Double findTotalPriceByVendorId(@Param("id") int vendorId);
}
