package org.eclipse.cargotracker.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Qualifier;

@ApplicationScoped
public class AppConfiguration {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    public @interface GraphTraversalUrl {}

    @Produces
    @GraphTraversalUrl
    public String graphTraversalUrl() {
        return "http://localhost:8080/cargo-tracker/rest/graph-traversal/shortest-path";
    }
}
