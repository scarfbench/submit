package org.eclipse.cargotracker.infrastructure.events.cdi;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marker annotation. In the Spring Boot version, cargo updates are handled via
 * Spring ApplicationEvents instead of CDI events.
 */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface CargoUpdated {}
