package com.example.addressbook.repository;

import com.example.addressbook.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Contact entities.
 * Replaces the Quarkus EJB ContactFacade and AbstractFacade.
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
}
