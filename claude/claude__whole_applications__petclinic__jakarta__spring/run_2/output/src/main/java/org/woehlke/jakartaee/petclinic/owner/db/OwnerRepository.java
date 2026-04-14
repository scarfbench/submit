package org.woehlke.jakartaee.petclinic.owner.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    @Query("select o from Owner o where o.searchindex like :searchterm order by o.lastName, o.firstName asc")
    List<Owner> search(@Param("searchterm") String searchterm);

    List<Owner> findAllByOrderByLastNameAscFirstNameAsc();
}
