package io.github.raeperd.realworld.application.security;

import io.github.raeperd.realworld.infrastructure.jwt.UserJWTPayload;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class AuthContext {

    private UserJWTPayload payload;
    private String token;

    public UserJWTPayload getPayload() {
        return payload;
    }

    public void setPayload(UserJWTPayload payload) {
        this.payload = payload;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isAuthenticated() {
        return payload != null;
    }

    public long getUserId() {
        if (payload == null) {
            throw new SecurityException("Not authenticated");
        }
        return payload.getUserId();
    }
}
