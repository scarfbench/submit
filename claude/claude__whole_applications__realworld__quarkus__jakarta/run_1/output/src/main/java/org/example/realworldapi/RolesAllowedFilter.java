package org.example.realworldapi;

import jakarta.annotation.Priority;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Method;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RolesAllowedFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Method method = resourceInfo.getResourceMethod();
        if (method == null) return;

        // Check for @PermitAll - allow all
        if (method.isAnnotationPresent(PermitAll.class)) {
            return;
        }

        // Check for @RolesAllowed
        RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            // Check class level
            rolesAllowed = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
        }

        if (rolesAllowed == null) {
            // No security annotation - allow
            return;
        }

        // Require authentication
        if (requestContext.getSecurityContext().getUserPrincipal() == null) {
            throw new NotAuthorizedException("Bearer");
        }

        // Check roles
        String[] roles = rolesAllowed.value();
        for (String role : roles) {
            if (requestContext.getSecurityContext().isUserInRole(role)) {
                return;
            }
        }

        throw new ForbiddenException("Insufficient permissions");
    }
}
