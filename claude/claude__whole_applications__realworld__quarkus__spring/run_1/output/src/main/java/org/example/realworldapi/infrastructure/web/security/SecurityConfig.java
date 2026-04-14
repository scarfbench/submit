package org.example.realworldapi.infrastructure.web.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Actuator
                        .requestMatchers("/actuator/**").permitAll()
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/users", "/users/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/articles", "/articles/{slug}", "/articles/{slug}/comments", "/profiles/{username}", "/tags").permitAll()
                        // Authenticated endpoints
                        .requestMatchers(HttpMethod.GET, "/user", "/articles/feed").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/user").authenticated()
                        .requestMatchers(HttpMethod.POST, "/articles", "/articles/{slug}/comments", "/articles/{slug}/favorite", "/profiles/{username}/follow").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/articles/{slug}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/articles/{slug}", "/articles/{slug}/comments/{id}", "/articles/{slug}/favorite", "/profiles/{username}/follow").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
