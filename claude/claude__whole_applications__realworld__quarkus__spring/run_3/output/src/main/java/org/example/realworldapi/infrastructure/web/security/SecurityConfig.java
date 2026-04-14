package org.example.realworldapi.infrastructure.web.security;

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
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.POST, "/users", "/users/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/articles", "/articles/*", "/articles/*/comments", "/profiles/*", "/tags").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Authenticated endpoints
                .requestMatchers(HttpMethod.GET, "/user").hasRole("USER")
                .requestMatchers(HttpMethod.PUT, "/user").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/articles/feed").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/articles").hasRole("USER")
                .requestMatchers(HttpMethod.PUT, "/articles/*").hasRole("USER")
                .requestMatchers(HttpMethod.DELETE, "/articles/*").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/articles/*/comments").hasRole("USER")
                .requestMatchers(HttpMethod.DELETE, "/articles/*/comments/*").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/articles/*/favorite").hasRole("USER")
                .requestMatchers(HttpMethod.DELETE, "/articles/*/favorite").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/profiles/*/follow").hasRole("USER")
                .requestMatchers(HttpMethod.DELETE, "/profiles/*/follow").hasRole("USER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
