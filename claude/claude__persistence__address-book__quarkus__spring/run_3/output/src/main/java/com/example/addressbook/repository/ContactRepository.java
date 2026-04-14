package com.example.addressbook.repository;

import com.example.addressbook.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Contact entity.
 * Replaces the Quarkus AbstractFacade/ContactFacade EJB pattern.
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Page<Contact> findAll(Pageable pageable);
}
