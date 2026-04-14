package org.eclipse.cargotracker.application.util;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SampleDataGenerator {

    private final InitLoader loader;

    public SampleDataGenerator(InitLoader loader) {
        this.loader = loader;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadSampleData() {
        loader.loadData();
    }
}
