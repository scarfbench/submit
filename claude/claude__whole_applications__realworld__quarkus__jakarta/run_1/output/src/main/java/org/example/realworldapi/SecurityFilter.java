package org.example.realworldapi;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityFilter implements ContainerRequestFilter {

    private RSAPublicKey publicKey;

    public SecurityFilter() {
        try {
            publicKey = loadPublicKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    private RSAPublicKey loadPublicKey() throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("publicKey.pem")) {
            if (is == null) {
                throw new RuntimeException("publicKey.pem not found on classpath");
            }
            String pem = new String(is.readAllBytes());
            pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                      .replace("-----END PUBLIC KEY-----", "")
                      .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(pem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                SignedJWT signedJWT = SignedJWT.parse(token);
                JWSVerifier verifier = new RSASSAVerifier(publicKey);
                if (signedJWT.verify(verifier)) {
                    JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
                    String subject = claims.getSubject();
                    List<String> groups = claims.getStringListClaim("groups");
                    if (groups == null) {
                        groups = List.of("USER");
                    }
                    final String sub = subject;
                    final Set<String> roles = new HashSet<>(groups);
                    requestContext.setSecurityContext(new jakarta.ws.rs.core.SecurityContext() {
                        @Override
                        public java.security.Principal getUserPrincipal() {
                            return () -> sub;
                        }
                        @Override
                        public boolean isUserInRole(String role) {
                            return roles.contains(role);
                        }
                        @Override
                        public boolean isSecure() {
                            return false;
                        }
                        @Override
                        public String getAuthenticationScheme() {
                            return "Bearer";
                        }
                    });
                }
            } catch (Exception e) {
                // Invalid token - proceed without authentication
            }
        }
    }
}
