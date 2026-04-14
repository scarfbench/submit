package org.example.realworldapi.infrastructure.provider;

import org.example.realworldapi.domain.model.provider.HashProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptHashProvider implements HashProvider {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hashPassword(String password) {
        return encoder.encode(password);
    }

    @Override
    public boolean checkPassword(String plaintext, String hashed) {
        return encoder.matches(plaintext, hashed);
    }
}
