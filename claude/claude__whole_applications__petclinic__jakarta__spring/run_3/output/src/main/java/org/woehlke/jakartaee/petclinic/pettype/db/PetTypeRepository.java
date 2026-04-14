package org.woehlke.jakartaee.petclinic.pettype.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.pettype.PetType;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetTypeRepository extends JpaRepository<PetType, Long> {

    List<PetType> findAllByOrderByNameAsc();

    Optional<PetType> findByName(String name);

    @Query("select v from PetType v where v.searchindex like %:searchterm% order by v.name ASC")
    List<PetType> search(@Param("searchterm") String searchterm);
}
