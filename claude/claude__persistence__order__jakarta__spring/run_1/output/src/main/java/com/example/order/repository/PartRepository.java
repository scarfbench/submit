package com.example.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.order.entity.Part;
import com.example.order.entity.PartKey;

@Repository
public interface PartRepository extends JpaRepository<Part, PartKey> {

    List<Part> findAllByOrderByPartNumberAsc();
}
