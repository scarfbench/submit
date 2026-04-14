package org.woehlke.jakartaee.petclinic.pettype.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.pettype.PetType;

import java.util.List;

@Repository
public interface PetTypeRepository extends JpaRepository<PetType, Long> {

    @Query("SELECT pt FROM PetType pt ORDER BY pt.name")
    List<PetType> findAllOrderByName();

    PetType findByName(String name);

    @Query("SELECT pt FROM PetType pt WHERE pt.searchindex LIKE :searchterm ORDER BY pt.name")
    List<PetType> search(@Param("searchterm") String searchterm);
}
