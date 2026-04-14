package org.example.realworldapi.infrastructure.web.security.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.realworldapi.infrastructure.web.provider.TokenProvider;
import org.example.realworldapi.infrastructure.web.security.annotation.Authenticated;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationFilter implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER_PREFIX = "Token ";
    private final TokenProvider tokenProvider;

    public AuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Authenticated methodAuth = handlerMethod.getMethodAnnotation(Authenticated.class);
        Authenticated classAuth = handlerMethod.getBeanType().getAnnotation(Authenticated.class);

        // No auth annotation - allow through
        if (methodAuth == null && classAuth == null) {
            return true;
        }

        boolean optional = methodAuth != null ? methodAuth.optional() : classAuth.optional();

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
            String token = authHeader.substring(AUTHORIZATION_HEADER_PREFIX.length());
            try {
                DecodedJWT decodedJWT = tokenProvider.verify(token);
                request.setAttribute("loggedUserId", java.util.UUID.fromString(decodedJWT.getSubject()));
            } catch (JWTVerificationException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"errors\":{\"body\":[\"Unauthorized\"]}}");
                return false;
            }
        } else if (!optional) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"errors\":{\"body\":[\"Unauthorized\"]}}");
            return false;
        }

        return true;
    }
}
