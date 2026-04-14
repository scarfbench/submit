package org.woehlke.jakartaee.petclinic.vet.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.vet.Vet;

import java.util.List;

@Repository
public interface VetRepository extends JpaRepository<Vet, Long> {

    @Query("SELECT v FROM Vet v ORDER BY v.lastName, v.firstName")
    List<Vet> findAllOrderByName();

    @Query("SELECT v FROM Vet v WHERE v.searchindex LIKE :searchterm ORDER BY v.lastName, v.firstName")
    List<Vet> search(@Param("searchterm") String searchterm);
}
