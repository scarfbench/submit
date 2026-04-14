package org.eclipse.cargotracker.application;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

/** Loads sample data for demo. */
@Singleton
@Startup
public class BookingServiceTestDataGenerator {

  @Inject
  private TestDataGenerator generator;

  @PostConstruct
  public void loadSampleData() {
    // can't have @Transactional for calls inside the same class
    generator.loadData();
  }

}
