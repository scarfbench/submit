package org.eclipse.cargotracker.application;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

/** Loads sample data for tests. */
@Singleton
@Startup
public class BookingServiceTestDataGenerator {

  @Inject
  private TestDataGenerator generator;

  @PostConstruct
  public void loadSampleData() {
    generator.loadData();
  }
}
