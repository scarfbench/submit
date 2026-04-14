package org.example.realworldapi.infrastructure.web.security.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.realworldapi.infrastructure.web.provider.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationFilter implements HandlerInterceptor {

    private final String AUTHORIZATION_HEADER_PREFIX = "Token ";

    @Autowired
    private TokenProvider tokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
            String token = authorizationHeader.replace(AUTHORIZATION_HEADER_PREFIX, "");
            try {
                DecodedJWT decodedJWT = tokenProvider.verify(token);
                request.setAttribute("loggedUserId", java.util.UUID.fromString(decodedJWT.getSubject()));
                request.setAttribute("decodedJWT", decodedJWT);
            } catch (JWTVerificationException ex) {
                // Invalid token - don't set user attributes
            }
        }

        return true;
    }
}
