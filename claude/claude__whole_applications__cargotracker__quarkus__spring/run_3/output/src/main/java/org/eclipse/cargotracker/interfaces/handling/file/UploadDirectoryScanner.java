package org.eclipse.cargotracker.interfaces.handling.file;

// import io.quarkus.scheduler.Scheduled;
// import jakarta.batch.operations.JobOperator;
// import jakarta.batch.runtime.BatchRuntime;
// import jakarta.enterprise.context.ApplicationScoped;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations from the contents by calling a batch job.
 *
 * <p>
 * Files that fail to parse are moved into a separate directory, successful files are deleted.
 *
 * <p>
 * NOTE: Quarkus Scheduler and JBeret batch functionality has been commented out for Spring Boot migration.
 * This class structure is preserved but batch operations are disabled.
 * Consider using Spring Batch as a replacement if batch processing is needed.
 */
@Component
// @ApplicationScoped // CDI scope - replaced with Spring @Component
@Transactional
public class UploadDirectoryScanner {

  @Scheduled(fixedRate = 120000) // In production, run every fifteen minutes (120000ms = 2 minutes)
  public void processFiles() {
    // JBeret batch functionality commented out
    // JobOperator jobOperator = BatchRuntime.getJobOperator();
    // jobOperator.start("EventFilesProcessorJob", null);
  }
}
