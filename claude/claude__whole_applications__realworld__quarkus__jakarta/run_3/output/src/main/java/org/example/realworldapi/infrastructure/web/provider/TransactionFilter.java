package org.example.realworldapi.infrastructure.web.provider;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.USER - 100)
public class TransactionFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    private EntityManager em;

    @Context
    private ResourceInfo resourceInfo;

    private static final ThreadLocal<Boolean> TX_ACTIVE = new ThreadLocal<>();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (resourceInfo.getResourceMethod() != null
                && resourceInfo.getResourceMethod().isAnnotationPresent(Transactional.class)) {
            em.getTransaction().begin();
            TX_ACTIVE.set(true);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        if (Boolean.TRUE.equals(TX_ACTIVE.get())) {
            TX_ACTIVE.remove();
            try {
                if (em.getTransaction().isActive()) {
                    if (responseContext.getStatus() >= 400) {
                        em.getTransaction().rollback();
                    } else {
                        em.getTransaction().commit();
                    }
                }
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    try {
                        em.getTransaction().rollback();
                    } catch (Exception ex) {
                        // ignore
                    }
                }
                throw new RuntimeException(e);
            }
        }
    }
}
