package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateConverter;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Batch item writer for handling events.
 * Jakarta Batch API annotations removed for Spring Boot compatibility.
 * Keep as plain component - integrate with Spring Batch if batch processing is needed.
 */
@Component
public class EventItemWriter {

  private static final String ARCHIVE_DIRECTORY = "archive_directory";

  @Autowired
  private ApplicationEvents applicationEvents;

  private String archiveDirectory;
  private String jobName;
  private long instanceId;

  public void open(Serializable checkpoint, String archiveDir, String jobName, long instanceId) throws Exception {
    this.archiveDirectory = archiveDir;
    this.jobName = jobName;
    this.instanceId = instanceId;

    File archiveDir2 = new File(archiveDir);
    if (!archiveDir2.exists()) {
      archiveDir2.mkdirs();
    }
  }

  @Transactional
  public void writeItems(List<Object> items) throws Exception {
    try (PrintWriter archive =
        new PrintWriter(
            new BufferedWriter(
                new FileWriter(
                    archiveDirectory
                        + "/archive_"
                        + jobName
                        + "_"
                        + instanceId
                        + ".csv",
                    true)))) {

      items
          .stream()
          .map(item -> (HandlingEventRegistrationAttempt) item)
          .forEach(
              attempt -> {
                applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
                archive.println(
                    DateConverter.toString(attempt.getRegistrationTime())
                        + ","
                        + DateConverter.toString(attempt.getCompletionTime())
                        + ","
                        + attempt.getTrackingId()
                        + ","
                        + attempt.getVoyageNumber()
                        + ","
                        + attempt.getUnLocode()
                        + ","
                        + attempt.getType());
              });
    }
  }
}
