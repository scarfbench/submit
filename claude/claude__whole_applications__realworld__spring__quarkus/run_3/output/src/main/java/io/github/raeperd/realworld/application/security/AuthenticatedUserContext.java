package io.github.raeperd.realworld.application.security;

import io.github.raeperd.realworld.infrastructure.jwt.UserJWTPayload;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class AuthenticatedUserContext {

    private UserJWTPayload jwtPayload;
    private String jwtToken;

    public UserJWTPayload getJwtPayload() {
        return jwtPayload;
    }

    public void setJwtPayload(UserJWTPayload jwtPayload) {
        this.jwtPayload = jwtPayload;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public boolean isAuthenticated() {
        return jwtPayload != null;
    }
}
