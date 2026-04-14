package org.eclipse.cargotracker.infrastructure.events.cdi;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.springframework.context.ApplicationEvent;

public class CargoUpdated extends ApplicationEvent {
    private final Cargo cargo;

    public CargoUpdated(Object source, Cargo cargo) {
        super(source);
        this.cargo = cargo;
    }

    public Cargo getCargo() {
        return cargo;
    }
}
