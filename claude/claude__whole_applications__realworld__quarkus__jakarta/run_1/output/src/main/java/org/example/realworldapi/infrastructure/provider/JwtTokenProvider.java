package org.example.realworldapi.infrastructure.provider;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

@ApplicationScoped
public class JwtTokenProvider {

  private RSAPrivateKey privateKey;

  @PostConstruct
  public void init() {
    try {
      privateKey = loadPrivateKey();
    } catch (Exception e) {
      throw new RuntimeException("Failed to load private key", e);
    }
  }

  private RSAPrivateKey loadPrivateKey() throws Exception {
    try (InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("privateKey.pem")) {
      if (is == null) {
        throw new RuntimeException("privateKey.pem not found on classpath");
      }
      String pem = new String(is.readAllBytes());
      pem =
          pem.replace("-----BEGIN PRIVATE KEY-----", "")
              .replace("-----END PRIVATE KEY-----", "")
              .replaceAll("\\s", "");
      byte[] decoded = Base64.getDecoder().decode(pem);
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return (RSAPrivateKey) kf.generatePrivate(spec);
    }
  }

  public String createUserToken(String subject) {
    try {
      JWTClaimsSet claims =
          new JWTClaimsSet.Builder()
              .issuer("users-service")
              .subject(subject)
              .claim("groups", List.of("USER"))
              .expirationTime(new Date(System.currentTimeMillis() + 86400000))
              .issueTime(new Date())
              .build();

      JWSHeader header =
          new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build();

      SignedJWT signedJWT = new SignedJWT(header, claims);
      signedJWT.sign(new RSASSASigner(privateKey));

      return signedJWT.serialize();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create JWT token", e);
    }
  }
}
