package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
// import jakarta.batch.api.chunk.listener.SkipReadListener;
// import jakarta.batch.runtime.context.JobContext;
// import jakarta.enterprise.context.Dependent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * NOTE: JBeret/Jakarta Batch-specific functionality has been commented out for Spring Boot migration.
 * This class structure is preserved but batch operations are disabled.
 * Consider using Spring Batch as a replacement if batch processing is needed.
 */
@Component("LineParseExceptionListener")
// @Dependent // CDI scope - replaced with Spring @Component
public class LineParseExceptionListener /* implements SkipReadListener */ {

  private static final String FAILED_DIRECTORY = "failed_directory";

  private static final Logger logger = Logger.getLogger(LineParseExceptionListener.class.getName());

  // @Inject
  // private JobContext jobContext; // JBeret-specific - commented out

  // @Override
  public void onSkipReadItem(Exception e) throws Exception {
    // JBeret batch functionality commented out
    // File failedDirectory = new File(jobContext.getProperties().getProperty(FAILED_DIRECTORY));
    //
    // if (!failedDirectory.exists()) {
    //   failedDirectory.mkdirs();
    // }
    //
    // EventLineParseException parseException = (EventLineParseException) e;
    //
    // logger.log(Level.WARNING, "Problem parsing event file line", parseException);
    //
    // try (PrintWriter failed =
    //     new PrintWriter(
    //         new BufferedWriter(
    //             new FileWriter(
    //                 new File(
    //                     failedDirectory,
    //                     "failed_"
    //                         + jobContext.getJobName()
    //                         + "_"
    //                         + jobContext.getInstanceId()
    //                         + ".csv"),
    //                 true)))) {
    //   failed.println(parseException.getLine());
    // }
  }
}
