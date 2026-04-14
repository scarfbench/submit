package org.eclipse.cargotracker.application.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Loads sample data for demo. */
@Component
public class SampleDataGenerator {

  @Autowired
  private InitLoader loader;

  @EventListener(ApplicationReadyEvent.class)
  public void loadSampleData() {
    loader.loadData();
  }
}
