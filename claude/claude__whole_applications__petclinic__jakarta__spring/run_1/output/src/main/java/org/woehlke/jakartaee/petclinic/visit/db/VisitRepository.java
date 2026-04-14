package org.woehlke.jakartaee.petclinic.visit.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("SELECT v FROM Visit v ORDER BY v.date DESC")
    List<Visit> findAllOrderByDate();

    @Query("SELECT v FROM Visit v WHERE v.pet = :pet ORDER BY v.date DESC")
    List<Visit> findByPet(@Param("pet") Pet pet);
}
