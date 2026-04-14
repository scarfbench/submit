package org.woehlke.jakartaee.petclinic.specialty.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import java.util.List;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    @Query("SELECT s FROM Specialty s ORDER BY s.name")
    List<Specialty> findAllOrderByName();

    Specialty findByName(String name);

    @Query("SELECT s FROM Specialty s WHERE s.searchindex LIKE :searchterm ORDER BY s.name")
    List<Specialty> search(@Param("searchterm") String searchterm);
}
