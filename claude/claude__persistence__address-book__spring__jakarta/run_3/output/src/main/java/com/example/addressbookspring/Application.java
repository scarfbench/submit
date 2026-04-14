package com.example.addressbookspring;

import jakarta.ws.rs.ApplicationPath;

/**
 * JAX-RS application activator.
 * This replaces the Spring Boot main class.
 * Maps REST endpoints under /api/*
 */
@ApplicationPath("/api")
public class Application extends jakarta.ws.rs.core.Application {
}
