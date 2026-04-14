package org.example.realworldapi;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class EntityManagerProducer {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerProducer.class);
    private EntityManagerFactory emf;

    private static final ThreadLocal<EntityManager> threadLocalEm = new ThreadLocal<>();

    @PostConstruct
    public void init() {
        Map<String, String> props = new HashMap<>();

        // Allow environment variables to override persistence.xml settings
        String jdbcUrl = System.getenv("JDBC_URL");
        String dbUser = System.getenv("DB_USERNAME");
        String dbPass = System.getenv("DB_PASSWORD");

        if (jdbcUrl != null) {
            props.put("jakarta.persistence.jdbc.url", jdbcUrl);
        }
        if (dbUser != null) {
            props.put("jakarta.persistence.jdbc.user", dbUser);
        }
        if (dbPass != null) {
            props.put("jakarta.persistence.jdbc.password", dbPass);
        }

        emf = Persistence.createEntityManagerFactory("realworld", props);
        LOG.info("EntityManagerFactory created");
    }

    @Produces
    @Dependent
    public EntityManager createEntityManager() {
        EntityManager em = threadLocalEm.get();
        if (em == null || !em.isOpen()) {
            em = emf.createEntityManager();
            threadLocalEm.set(em);
        }
        return em;
    }

    /**
     * Get the current thread-local EntityManager.
     * Used by repositories to always get the current request's EM.
     */
    public static EntityManager getCurrentEntityManager() {
        return threadLocalEm.get();
    }

    /**
     * Close and remove the thread-local EntityManager.
     * Called by the TransactionalInterceptor after transaction completes.
     */
    public static void closeCurrentEntityManager() {
        EntityManager em = threadLocalEm.get();
        if (em != null) {
            if (em.isOpen()) {
                em.close();
            }
            threadLocalEm.remove();
        }
    }

    @PreDestroy
    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}
