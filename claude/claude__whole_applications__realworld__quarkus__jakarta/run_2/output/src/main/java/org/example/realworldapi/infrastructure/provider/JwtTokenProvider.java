package org.example.realworldapi.infrastructure.provider;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

@ApplicationScoped
public class JwtTokenProvider {

  private RSAPrivateKey privateKey;

  public JwtTokenProvider() {
    try {
      this.privateKey = loadPrivateKey();
    } catch (Exception e) {
      System.err.println("Warning: Could not load JWT private key: " + e.getMessage());
    }
  }

  public String createUserToken(String subject) {
    try {
      JWTClaimsSet claimsSet =
          new JWTClaimsSet.Builder()
              .issuer("users-service")
              .subject(subject)
              .claim("groups", List.of("USER"))
              .expirationTime(new Date(System.currentTimeMillis() + 3600 * 1000))
              .issueTime(new Date())
              .build();

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
      signedJWT.sign(new RSASSASigner(privateKey));
      return signedJWT.serialize();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create JWT token", e);
    }
  }

  private RSAPrivateKey loadPrivateKey() throws Exception {
    try (InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("privateKey.pem")) {
      if (is == null) {
        throw new RuntimeException("privateKey.pem not found in classpath");
      }
      String pem = new String(is.readAllBytes());
      pem =
          pem.replace("-----BEGIN PRIVATE KEY-----", "")
              .replace("-----END PRIVATE KEY-----", "")
              .replace("-----BEGIN RSA PRIVATE KEY-----", "")
              .replace("-----END RSA PRIVATE KEY-----", "")
              .replaceAll("\\s", "");
      byte[] decoded = Base64.getDecoder().decode(pem);
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return (RSAPrivateKey) kf.generatePrivate(spec);
    }
  }
}
