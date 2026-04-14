package com.coffeeshop.orders.domain;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderRepository {

  @Inject
  EntityManager em;

  @Transactional
  public Long save(OrderEntity e){
    em.persist(e);
    em.flush();
    return e.getId();
  }

  public OrderEntity find(long id){
    return em.find(OrderEntity.class, id);
  }
}
