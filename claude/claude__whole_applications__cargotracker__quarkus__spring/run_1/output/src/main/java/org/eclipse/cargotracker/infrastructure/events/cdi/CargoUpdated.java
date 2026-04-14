package org.eclipse.cargotracker.infrastructure.events.cdi;

/**
 * Marker annotation - in Spring, we use ApplicationEvent or simply publish
 * the Cargo object directly. This class is kept for backward compatibility references.
 * Spring event publishing uses ApplicationEventPublisher.publishEvent(cargo).
 */
public @interface CargoUpdated {
}
