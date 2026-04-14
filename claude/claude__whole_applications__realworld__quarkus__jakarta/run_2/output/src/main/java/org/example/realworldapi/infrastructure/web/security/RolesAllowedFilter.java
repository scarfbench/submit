package org.example.realworldapi.infrastructure.web.security;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Method;

@Provider
public class RolesAllowedFilter implements DynamicFeature {

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    Method method = resourceInfo.getResourceMethod();
    Class<?> clazz = resourceInfo.getResourceClass();

    // Check method-level annotations first, then class-level
    RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
    if (rolesAllowed == null) {
      rolesAllowed = clazz.getAnnotation(RolesAllowed.class);
    }

    PermitAll permitAll = method.getAnnotation(PermitAll.class);
    if (permitAll == null) {
      permitAll = clazz.getAnnotation(PermitAll.class);
    }

    if (rolesAllowed != null && permitAll == null) {
      context.register(new RolesCheckFilter(rolesAllowed.value()));
    }
  }

  private static class RolesCheckFilter implements ContainerRequestFilter {
    private final String[] allowedRoles;

    RolesCheckFilter(String[] allowedRoles) {
      this.allowedRoles = allowedRoles;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
      if (requestContext.getSecurityContext().getUserPrincipal() == null) {
        throw new NotAuthorizedException("Bearer");
      }

      for (String role : allowedRoles) {
        if (requestContext.getSecurityContext().isUserInRole(role)) {
          return;
        }
      }

      throw new ForbiddenException("Insufficient permissions");
    }
  }
}
