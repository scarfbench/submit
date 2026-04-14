package org.woehlke.jakartaee.petclinic.owner.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woehlke.jakartaee.petclinic.owner.Owner;

import java.util.List;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    @Query("SELECT o FROM Owner o ORDER BY o.lastName, o.firstName")
    List<Owner> findAllOrderByName();

    @Query("SELECT o FROM Owner o WHERE o.searchindex LIKE :searchterm ORDER BY o.lastName, o.firstName")
    List<Owner> search(@Param("searchterm") String searchterm);
}
