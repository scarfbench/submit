package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
// import jakarta.batch.api.chunk.AbstractItemWriter;
// import jakarta.batch.runtime.context.JobContext;
// import jakarta.enterprise.context.Dependent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateConverter;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

/**
 * NOTE: JBeret/Jakarta Batch-specific functionality has been commented out for Spring Boot migration.
 * This class structure is preserved but batch operations are disabled.
 * Consider using Spring Batch as a replacement if batch processing is needed.
 */
@Component("EventItemWriter")
// @Dependent // CDI scope - replaced with Spring @Component
public class EventItemWriter /* extends AbstractItemWriter */ {

  private static final String ARCHIVE_DIRECTORY = "archive_directory";

  // @Inject
  // private JobContext jobContext; // JBeret-specific - commented out
  @Autowired
  private ApplicationEvents applicationEvents;

  // @Override
  public void open(Serializable checkpoint) throws Exception {
    // JBeret batch functionality commented out
    // File archiveDirectory = new File(jobContext.getProperties().getProperty(ARCHIVE_DIRECTORY));
    //
    // if (!archiveDirectory.exists()) {
    //   archiveDirectory.mkdirs();
    // }
  }

  // @Override
  @Transactional
  public void writeItems(List<Object> items) throws Exception {
    // JBeret batch functionality commented out
    // try (PrintWriter archive =
    //     new PrintWriter(
    //         new BufferedWriter(
    //             new FileWriter(
    //                 jobContext.getProperties().getProperty(ARCHIVE_DIRECTORY)
    //                     + "/archive_"
    //                     + jobContext.getJobName()
    //                     + "_"
    //                     + jobContext.getInstanceId()
    //                     + ".csv",
    //                 true)))) {
    //
    //   items
    //       .stream()
    //       .map(item -> (HandlingEventRegistrationAttempt) item)
    //       .forEach(
    //           attempt -> {
    //             applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
    //             archive.println(
    //                 DateConverter.toString(attempt.getRegistrationTime())
    //                     + ","
    //                     + DateConverter.toString(attempt.getCompletionTime())
    //                     + ","
    //                     + attempt.getTrackingId()
    //                     + ","
    //                     + attempt.getVoyageNumber()
    //                     + ","
    //                     + attempt.getUnLocode()
    //                     + ","
    //                     + attempt.getType());
    //           });
    // }
  }
}
