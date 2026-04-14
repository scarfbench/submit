package org.woehlke.jakartaee.petclinic.pettype.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetTypeRepository extends JpaRepository<PetType, Long> {

    List<PetType> findAllByOrderByNameAsc();
}
