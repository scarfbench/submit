package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LineParseExceptionListener {

  @Value("${failed.directory:failed}")
  private String failedDirectory;

  @Autowired
  private Logger logger;

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
                        failedDir, "failed_" + System.currentTimeMillis() + ".csv"),
                    true)))) {
      failed.println(parseException.getLine());
    }
  }
}
