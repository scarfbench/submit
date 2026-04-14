package org.woehlke.jakartaee.petclinic.vet.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VetRepository extends JpaRepository<Vet, Long> {

    @Query("select v from Vet v where v.searchindex like :searchterm order by v.lastName, v.firstName")
    List<Vet> search(@Param("searchterm") String searchterm);

    List<Vet> findAllByOrderByLastNameAscFirstNameAsc();
}
