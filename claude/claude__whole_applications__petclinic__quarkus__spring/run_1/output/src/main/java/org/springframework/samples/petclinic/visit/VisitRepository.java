package org.springframework.samples.petclinic.visit;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    Collection<Visit> findByPetId(Long petId);
}
