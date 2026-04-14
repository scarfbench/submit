package org.example.realworldapi.infrastructure.web.provider;

import jakarta.annotation.Priority;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class SecurityFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Method method = resourceInfo.getResourceMethod();
        if (method == null) return;

        // Check for @PermitAll on method
        if (method.isAnnotationPresent(PermitAll.class)) {
            return; // Allow access
        }

        // Check for @RolesAllowed on method
        RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            // Check class level
            rolesAllowed = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
        }

        if (rolesAllowed != null) {
            // Need authentication
            if (requestContext.getSecurityContext().getUserPrincipal() == null) {
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"errors\":{\"body\":[\"unauthorized\"]}}")
                        .type("application/json")
                        .build()
                );
                return;
            }

            // Check roles
            boolean hasRole = false;
            for (String role : rolesAllowed.value()) {
                if (requestContext.getSecurityContext().isUserInRole(role)) {
                    hasRole = true;
                    break;
                }
            }
            if (!hasRole) {
                requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"errors\":{\"body\":[\"forbidden\"]}}")
                        .type("application/json")
                        .build()
                );
            }
        }
    }
}
