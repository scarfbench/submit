package org.example.realworldapi.infrastructure.transaction;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.realworldapi.EntityManagerProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Interceptor
@Transactional
@Priority(Interceptor.Priority.APPLICATION)
public class TransactionalInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionalInterceptor.class);

    @Inject
    private EntityManagerProducer emProducer;

    @AroundInvoke
    public Object manageTransaction(InvocationContext ctx) throws Exception {
        // Get or create the thread-local EntityManager via CDI producer
        EntityManager em = emProducer.createEntityManager();
        boolean startedTransaction = false;
        try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
                startedTransaction = true;
            }
            Object result = ctx.proceed();
            if (startedTransaction && em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            return result;
        } catch (Exception e) {
            if (startedTransaction && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            if (startedTransaction) {
                EntityManagerProducer.closeCurrentEntityManager();
            }
        }
    }
}
