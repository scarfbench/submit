package org.woehlke.jakartaee.petclinic.pet.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    @Query("SELECT p FROM Pet p ORDER BY p.name")
    List<Pet> findAllOrderByName();

    @Query("SELECT p FROM Pet p WHERE p.owner = :owner ORDER BY p.name")
    List<Pet> findByOwner(@Param("owner") Owner owner);
}
