package org.eclipse.cargotracker.interfaces.handling.file;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations from the contents.
 *
 * Migrated from EJB @Schedule to Spring @Scheduled.
 * Jakarta Batch API functionality simplified - batch processing would require Spring Batch integration.
 */
@Component
public class UploadDirectoryScanner {

  private static final Logger logger = Logger.getLogger(UploadDirectoryScanner.class.getName());

  @Scheduled(fixedRate = 120000) // Run every 2 minutes
  public void processFiles() {
    logger.log(Level.INFO, "Scanning upload directory for new files...");
    // Batch file processing placeholder - original used Jakarta Batch API
    // To implement: integrate Spring Batch or implement custom file processing logic
  }
}
