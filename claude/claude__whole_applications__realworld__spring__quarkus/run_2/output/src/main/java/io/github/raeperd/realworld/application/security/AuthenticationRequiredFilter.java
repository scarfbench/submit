package io.github.raeperd.realworld.application.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Authenticated
@Priority(Priorities.AUTHORIZATION)
public class AuthenticationRequiredFilter implements ContainerRequestFilter {

    @Inject
    SecurityIdentityHolder securityIdentityHolder;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!securityIdentityHolder.isAuthenticated()) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
