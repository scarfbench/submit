package org.woehlke.jakartaee.petclinic.vet.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.vet.Vet;

import java.util.List;

@Repository
public interface VetRepository extends JpaRepository<Vet, Long> {

    List<Vet> findAllByOrderByLastNameAscFirstNameAsc();

    @Query("select v from Vet v where v.searchindex like %:searchterm%")
    List<Vet> search(@Param("searchterm") String searchterm);
}
