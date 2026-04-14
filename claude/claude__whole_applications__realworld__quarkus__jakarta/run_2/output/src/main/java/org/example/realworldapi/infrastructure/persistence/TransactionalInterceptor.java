package org.example.realworldapi.infrastructure.persistence;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Interceptor
@Transactional
@Priority(Interceptor.Priority.APPLICATION)
public class TransactionalInterceptor {

  @Inject private EntityManager em;

  @AroundInvoke
  public Object manageTransaction(InvocationContext ctx) throws Exception {
    if (em.getTransaction().isActive()) {
      // Already in a transaction, just proceed
      return ctx.proceed();
    }

    em.getTransaction().begin();
    try {
      Object result = ctx.proceed();
      if (em.getTransaction().isActive()) {
        em.getTransaction().commit();
      }
      return result;
    } catch (Exception e) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
      throw e;
    }
  }
}
