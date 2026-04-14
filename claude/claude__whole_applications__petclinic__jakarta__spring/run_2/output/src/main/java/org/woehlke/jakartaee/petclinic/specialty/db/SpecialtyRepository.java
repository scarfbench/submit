package org.woehlke.jakartaee.petclinic.specialty.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    @Query("select s from Specialty s where s.searchindex like :searchterm order by s.name")
    List<Specialty> search(@Param("searchterm") String searchterm);

    List<Specialty> findAllByOrderByNameAsc();
}
