package com.addressbook.repository;

import com.addressbook.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for Contact entity.
 * Replaces the Quarkus EJB AbstractFacade + ContactFacade pattern.
 * Spring Data JPA automatically provides CRUD operations.
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
}
