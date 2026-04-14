package org.eclipse.cargotracker.interfaces.handling.file;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
// import jakarta.batch.api.listener.JobListener;
// import jakarta.enterprise.context.Dependent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * NOTE: JBeret/Jakarta Batch-specific functionality has been commented out for Spring Boot migration.
 * This class structure is preserved but batch operations are disabled.
 * Consider using Spring Batch as a replacement if batch processing is needed.
 */
@Component("FileProcessorJobListener")
// @Dependent // CDI scope - replaced with Spring @Component
public class FileProcessorJobListener /* implements JobListener */ {

  private static final Logger logger = Logger.getLogger(FileProcessorJobListener.class.getName());

  // @Override
  public void beforeJob() throws Exception {
    logger.log(Level.INFO, "Handling event file processor batch job starting at {0}", new Date());
  }

  // @Override
  public void afterJob() throws Exception {
    logger.log(Level.INFO, "Handling event file processor batch job completed at {0}", new Date());
  }
}
