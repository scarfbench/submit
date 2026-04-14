package org.eclipse.cargotracker.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Loads sample data for demo. */
@Component
public class BookingServiceTestDataGenerator {

  @Autowired
  private TestDataGenerator generator;

  @EventListener(ApplicationReadyEvent.class)
  public void loadSampleData() {
    // can't have @Transactional for calls inside the same class
    generator.loadData();
  }

}
