package org.woehlke.jakartaee.petclinic.visit.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.pet.db.Pet;

import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByPetOrderByDateAsc(Pet pet);
}
