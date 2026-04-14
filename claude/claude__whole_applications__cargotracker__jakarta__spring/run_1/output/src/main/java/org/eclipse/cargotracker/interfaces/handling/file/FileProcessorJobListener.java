package org.eclipse.cargotracker.interfaces.handling.file;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;

/**
 * Job listener for file processing batch jobs.
 * Jakarta Batch API annotations removed for Spring Boot compatibility.
 * Keep as plain component - integrate with Spring Batch if batch processing is needed.
 */
@Component
public class FileProcessorJobListener {

  private static final Logger logger = Logger.getLogger(FileProcessorJobListener.class.getName());

  public void beforeJob() throws Exception {
    logger.log(Level.INFO, "Handling event file processor batch job starting at {0}", new Date());
  }

  public void afterJob() throws Exception {
    logger.log(Level.INFO, "Handling event file processor batch job completed at {0}", new Date());
  }
}
