package org.springframework.samples.petclinic.owner;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetTypeRepository extends JpaRepository<PetType, Long> {

    Optional<PetType> findByName(String name);
}
