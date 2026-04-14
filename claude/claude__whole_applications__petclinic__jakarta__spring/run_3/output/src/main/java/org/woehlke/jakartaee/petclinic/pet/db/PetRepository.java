package org.woehlke.jakartaee.petclinic.pet.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.pet.Pet;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findAllByOrderByBirthDateAscNameAsc();

    List<Pet> findByOwnerOrderByNameAsc(Owner owner);
}
