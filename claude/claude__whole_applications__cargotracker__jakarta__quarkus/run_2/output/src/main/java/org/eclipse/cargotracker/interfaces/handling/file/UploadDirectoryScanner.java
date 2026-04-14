package org.eclipse.cargotracker.interfaces.handling.file;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.scheduler.Scheduled;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations from the contents.
 *
 * <p>Files that fail to parse are moved into a separate directory, successful files are archived.
 */
@ApplicationScoped
public class UploadDirectoryScanner {

    @Inject
    Logger logger;

    @Inject
    EventFileProcessor eventFileProcessor;

    @Scheduled(every = "120s")
    public void processFiles() {
        logger.log(Level.INFO, "Scanning for event files...");
        eventFileProcessor.processEventFiles();
    }
}
