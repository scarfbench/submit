package io.github.raeperd.realworld.infrastructure.repository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordEncoderService {

    public String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(10, rawPassword.toCharArray());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword).verified;
    }
}
