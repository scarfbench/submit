package org.example.realworldapi.infrastructure.provider;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;

@ApplicationScoped
public class JwtTokenProvider {

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            this.privateKey = loadPrivateKey("privateKey.pem");
            this.publicKey = loadPublicKey("publicKey.pem");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWT keys", e);
        }
    }

    public String createUserToken(String subject) {
        try {
            JWSSigner signer = new RSASSASigner(privateKey);

            Date now = new Date();
            Date expiry = new Date(now.getTime() + 86400000L); // 24 hours

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer("users-service")
                    .claim("groups", List.of("USER"))
                    .issueTime(now)
                    .expirationTime(expiry)
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                    claimsSet
            );
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JWT token", e);
        }
    }

    public String validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(publicKey);

            if (!signedJWT.verify(verifier)) {
                return null;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // Check expiration
            Date expiration = claims.getExpirationTime();
            if (expiration != null && expiration.before(new Date())) {
                return null;
            }

            // Check issuer
            if (!"users-service".equals(claims.getIssuer())) {
                return null;
            }

            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private RSAPrivateKey loadPrivateKey(String resourceName) throws Exception {
        String pem = loadResource(resourceName);
        pem = pem.replace("-----BEGIN RSA PRIVATE KEY-----", "")
                  .replace("-----END RSA PRIVATE KEY-----", "")
                  .replace("-----BEGIN PRIVATE KEY-----", "")
                  .replace("-----END PRIVATE KEY-----", "")
                  .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(pem);

        try {
            // Try PKCS8 format first
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (InvalidKeySpecException e) {
            // Fallback: try to parse as PKCS1 (RSA PRIVATE KEY)
            return parsePkcs1PrivateKey(decoded);
        }
    }

    private RSAPrivateKey parsePkcs1PrivateKey(byte[] pkcs1Bytes) throws Exception {
        // Wrap PKCS1 in PKCS8 structure
        byte[] pkcs8Header = new byte[] {
            0x30, (byte)0x82, 0, 0, // SEQUENCE + length placeholder
            0x02, 0x01, 0x00,       // version INTEGER 0
            0x30, 0x0d,             // SEQUENCE
            0x06, 0x09,             // OID
            0x2a, (byte)0x86, 0x48, (byte)0x86, (byte)0xf7, 0x0d, 0x01, 0x01, 0x01, // rsaEncryption
            0x05, 0x00,             // NULL
            0x04, (byte)0x82, 0, 0  // OCTET STRING + length placeholder
        };

        int totalLen = pkcs8Header.length + pkcs1Bytes.length;
        byte[] pkcs8 = new byte[totalLen];
        System.arraycopy(pkcs8Header, 0, pkcs8, 0, pkcs8Header.length);
        System.arraycopy(pkcs1Bytes, 0, pkcs8, pkcs8Header.length, pkcs1Bytes.length);

        // Fix lengths
        int seqLen = totalLen - 4;
        pkcs8[2] = (byte)(seqLen >> 8);
        pkcs8[3] = (byte)(seqLen & 0xff);

        int octLen = pkcs1Bytes.length;
        pkcs8[pkcs8Header.length - 2] = (byte)(octLen >> 8);
        pkcs8[pkcs8Header.length - 1] = (byte)(octLen & 0xff);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pkcs8);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    private RSAPublicKey loadPublicKey(String resourceName) throws Exception {
        String pem = loadResource(resourceName);
        pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                  .replace("-----END PUBLIC KEY-----", "")
                  .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                  .replace("-----END RSA PUBLIC KEY-----", "")
                  .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }

    private String loadResource(String name) throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + name);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
