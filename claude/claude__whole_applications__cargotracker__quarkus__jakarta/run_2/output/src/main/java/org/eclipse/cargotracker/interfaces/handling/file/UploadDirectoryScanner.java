package org.eclipse.cargotracker.interfaces.handling.file;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations from the contents by calling a batch job.
 */
@Singleton
public class UploadDirectoryScanner {

  @Schedule(minute = "*/2", hour = "*", persistent = false)
  public void processFiles() {
    JobOperator jobOperator = BatchRuntime.getJobOperator();
    jobOperator.start("EventFilesProcessorJob", null);
  }
}
