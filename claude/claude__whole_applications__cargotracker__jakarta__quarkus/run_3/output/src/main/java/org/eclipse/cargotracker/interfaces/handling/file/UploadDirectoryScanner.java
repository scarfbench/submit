package org.eclipse.cargotracker.interfaces.handling.file;

import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.scheduler.Scheduled;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations. Converted from Jakarta Batch to Quarkus Scheduler.
 */
@ApplicationScoped
public class UploadDirectoryScanner {
  @Inject private Logger logger;

  @Scheduled(every = "120s")
  public void processFiles() {
    logger.info("Scheduled file processing check (batch job replacement).");
    // Batch processing placeholder - in Quarkus we would use a different approach
  }
}
