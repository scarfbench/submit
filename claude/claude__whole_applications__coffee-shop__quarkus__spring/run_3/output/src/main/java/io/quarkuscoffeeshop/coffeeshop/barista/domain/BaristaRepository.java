package io.quarkuscoffeeshop.coffeeshop.barista.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaristaRepository extends JpaRepository<BaristaItem, Long> {
}
