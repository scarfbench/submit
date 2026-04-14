package org.eclipse.cargotracker.application.util;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

/** Loads sample data for demo. */
@Singleton
@Startup
public class SampleDataGenerator {

  @Inject
  private InitLoader loader;

  @PostConstruct
  public void loadSampleData() {
    loader.loadData();
  }
}
