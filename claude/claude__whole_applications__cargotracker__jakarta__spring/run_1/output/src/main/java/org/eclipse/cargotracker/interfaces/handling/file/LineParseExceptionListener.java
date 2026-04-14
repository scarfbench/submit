package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;

/**
 * Listener for handling parse exceptions during batch processing.
 * Jakarta Batch API annotations removed for Spring Boot compatibility.
 * Keep as plain component - integrate with Spring Batch if batch processing is needed.
 */
@Component
public class LineParseExceptionListener {

  private static final String FAILED_DIRECTORY = "failed_directory";

  private static final Logger logger = Logger.getLogger(LineParseExceptionListener.class.getName());

  private String failedDirectory;
  private String jobName;
  private long instanceId;

  public void configure(String failedDir, String jobName, long instanceId) {
    this.failedDirectory = failedDir;
    this.jobName = jobName;
    this.instanceId = instanceId;
  }

  public void onSkipReadItem(Exception e) throws Exception {
    File failedDir = new File(failedDirectory);

    if (!failedDir.exists()) {
      failedDir.mkdirs();
    }

    EventLineParseException parseException = (EventLineParseException) e;

    logger.log(Level.WARNING, "Problem parsing event file line", parseException);

    try (PrintWriter failed =
        new PrintWriter(
            new BufferedWriter(
                new FileWriter(
                    new File(
                        failedDir,
                        "failed_"
                            + jobName
                            + "_"
                            + instanceId
                            + ".csv"),
                    true)))) {
      failed.println(parseException.getLine());
    }
  }
}
