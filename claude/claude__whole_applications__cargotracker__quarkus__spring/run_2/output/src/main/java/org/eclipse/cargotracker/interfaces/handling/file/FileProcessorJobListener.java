package org.eclipse.cargotracker.interfaces.handling.file;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileProcessorJobListener {

  @Autowired
  private Logger logger;

  public void beforeJob() throws Exception {
    logger.log(Level.INFO, "Handling event file processor batch job starting at {0}", new Date());
  }

  public void afterJob() throws Exception {
    logger.log(Level.INFO, "Handling event file processor batch job completed at {0}", new Date());
  }
}
