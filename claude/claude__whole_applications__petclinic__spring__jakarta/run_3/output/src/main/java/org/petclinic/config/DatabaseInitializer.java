package org.petclinic.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
public class DatabaseInitializer {

    @Resource(lookup = "java:jboss/datasources/PetClinicDS")
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        try {
            executeSqlFile("db/h2/schema.sql");
            executeSqlFile("db/h2/data.sql");
            System.out.println("Database initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeSqlFile(String resourcePath) throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            System.err.println("SQL file not found: " + resourcePath);
            return;
        }
        String sql;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            sql = reader.lines().collect(Collectors.joining("\n"));
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        stmt.execute(trimmed);
                    } catch (Exception e) {
                        // Ignore errors for DROP IF EXISTS etc.
                        System.out.println("SQL warning: " + e.getMessage());
                    }
                }
            }
        }
    }
}
