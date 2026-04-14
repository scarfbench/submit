package org.eclipse.cargotracker.infrastructure.events.cdi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.beans.factory.annotation.Qualifier;

@Qualifier
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface CargoUpdated {
}
