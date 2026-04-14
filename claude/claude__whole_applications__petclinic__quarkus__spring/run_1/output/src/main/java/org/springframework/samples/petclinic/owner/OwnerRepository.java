package org.springframework.samples.petclinic.owner;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Collection<Owner> findByLastName(String lastName);
}
