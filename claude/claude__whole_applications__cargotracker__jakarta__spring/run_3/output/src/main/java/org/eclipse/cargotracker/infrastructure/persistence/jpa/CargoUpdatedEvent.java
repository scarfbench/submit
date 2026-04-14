package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.springframework.context.ApplicationEvent;

public class CargoUpdatedEvent extends ApplicationEvent {

  private final Cargo cargo;

  public CargoUpdatedEvent(Cargo cargo) {
    super(cargo);
    this.cargo = cargo;
  }

  public Cargo getCargo() {
    return cargo;
  }
}
