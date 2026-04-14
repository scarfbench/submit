package org.example.realworldapi.infrastructure.web.provider;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import org.example.realworldapi.infrastructure.provider.JwtTokenProvider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    @Inject
    private JwtTokenProvider tokenProvider;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null) {
            String token = null;
            if (authHeader.startsWith("Token ")) {
                token = authHeader.substring(6).trim();
            } else if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
            }

            if (token != null && !token.isEmpty()) {
                try {
                    final String subject = tokenProvider.validateToken(token);
                    if (subject != null) {
                        requestContext.setSecurityContext(new SecurityContext() {
                            @Override
                            public Principal getUserPrincipal() {
                                return () -> subject;
                            }

                            @Override
                            public boolean isUserInRole(String role) {
                                return "USER".equals(role) || "ADMIN".equals(role);
                            }

                            @Override
                            public boolean isSecure() {
                                return requestContext.getUriInfo().getBaseUri().getScheme().equals("https");
                            }

                            @Override
                            public String getAuthenticationScheme() {
                                return "Token";
                            }
                        });
                    }
                } catch (Exception e) {
                    // Invalid token - continue without authentication
                }
            }
        }
    }
}
