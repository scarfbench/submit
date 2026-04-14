package org.example.realworldapi.infrastructure.web.security;

import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.List;

public class JwtSecurityContext implements SecurityContext {

  private final String subject;
  private final List<String> roles;
  private final SecurityContext originalContext;

  public JwtSecurityContext(String subject, List<String> roles, SecurityContext originalContext) {
    this.subject = subject;
    this.roles = roles;
    this.originalContext = originalContext;
  }

  @Override
  public Principal getUserPrincipal() {
    return () -> subject;
  }

  @Override
  public boolean isUserInRole(String role) {
    return roles != null && roles.contains(role);
  }

  @Override
  public boolean isSecure() {
    return originalContext != null && originalContext.isSecure();
  }

  @Override
  public String getAuthenticationScheme() {
    return "Bearer";
  }
}
