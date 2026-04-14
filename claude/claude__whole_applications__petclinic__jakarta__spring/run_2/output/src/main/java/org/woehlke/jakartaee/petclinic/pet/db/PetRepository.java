package org.woehlke.jakartaee.petclinic.pet.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.owner.db.Owner;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByOwnerOrderByNameAsc(Owner owner);
}
