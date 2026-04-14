package org.woehlke.jakartaee.petclinic.specialty.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    List<Specialty> findAllByOrderByNameAsc();

    Optional<Specialty> findByName(String name);

    @Query("select v from Specialty v where v.searchindex like %:searchterm% order by v.name asc")
    List<Specialty> search(@Param("searchterm") String searchterm);
}
