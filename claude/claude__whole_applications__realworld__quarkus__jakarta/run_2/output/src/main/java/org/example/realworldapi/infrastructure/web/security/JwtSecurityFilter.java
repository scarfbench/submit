package org.example.realworldapi.infrastructure.web.security;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtSecurityFilter implements ContainerRequestFilter {

  private RSAPublicKey publicKey;

  public JwtSecurityFilter() {
    try {
      this.publicKey = loadPublicKey();
    } catch (Exception e) {
      System.err.println("Warning: Could not load JWT public key: " + e.getMessage());
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String authHeader = requestContext.getHeaderString("Authorization");
    if (authHeader != null && authHeader.startsWith("Token ")) {
      String token = authHeader.substring(6).trim();
      try {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (publicKey != null) {
          JWSVerifier verifier = new RSASSAVerifier(publicKey);
          if (signedJWT.verify(verifier)) {
            String subject = signedJWT.getJWTClaimsSet().getSubject();
            List<String> groups =
                signedJWT.getJWTClaimsSet().getStringListClaim("groups");
            if (groups == null) {
              groups = List.of("USER");
            }
            requestContext.setSecurityContext(
                new JwtSecurityContext(subject, groups, requestContext.getSecurityContext()));
          }
        }
      } catch (Exception e) {
        // Invalid token - continue without authentication
      }
    } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7).trim();
      try {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (publicKey != null) {
          JWSVerifier verifier = new RSASSAVerifier(publicKey);
          if (signedJWT.verify(verifier)) {
            String subject = signedJWT.getJWTClaimsSet().getSubject();
            List<String> groups =
                signedJWT.getJWTClaimsSet().getStringListClaim("groups");
            if (groups == null) {
              groups = List.of("USER");
            }
            requestContext.setSecurityContext(
                new JwtSecurityContext(subject, groups, requestContext.getSecurityContext()));
          }
        }
      } catch (Exception e) {
        // Invalid token - continue without authentication
      }
    }
  }

  private RSAPublicKey loadPublicKey() throws Exception {
    try (InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("publicKey.pem")) {
      if (is == null) {
        throw new RuntimeException("publicKey.pem not found in classpath");
      }
      String pem = new String(is.readAllBytes());
      pem =
          pem.replace("-----BEGIN PUBLIC KEY-----", "")
              .replace("-----END PUBLIC KEY-----", "")
              .replaceAll("\\s", "");
      byte[] decoded = Base64.getDecoder().decode(pem);
      X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return (RSAPublicKey) kf.generatePublic(spec);
    }
  }
}
