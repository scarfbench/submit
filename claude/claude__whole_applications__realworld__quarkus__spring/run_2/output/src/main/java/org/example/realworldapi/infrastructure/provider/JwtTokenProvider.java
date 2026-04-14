package org.example.realworldapi.infrastructure.provider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final String SECRET = "realworld-api-secret-key-that-is-long-enough-for-hmac-sha256-algorithm-minimum-32-bytes";
  private static final String ISSUER = "users-service";

  private Key getSigningKey() {
    return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
  }

  public String createUserToken(String subject) {
    return Jwts.builder()
        .issuer(ISSUER)
        .subject(subject)
        .claim("groups", List.of("USER"))
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
        .signWith(getSigningKey())
        .compact();
  }

  public Optional<String> validateTokenAndGetSubject(String token) {
    try {
      var claims = Jwts.parser()
          .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
          .build()
          .parseSignedClaims(token)
          .getPayload();
      return Optional.ofNullable(claims.getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
