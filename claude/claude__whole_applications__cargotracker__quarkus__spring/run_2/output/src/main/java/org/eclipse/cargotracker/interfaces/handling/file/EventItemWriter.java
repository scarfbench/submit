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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EventItemWriter {

  @Value("${archive.directory:archive}")
  private String archiveDirectory;

  @Autowired
  private ApplicationEvents applicationEvents;

  public void open(Serializable checkpoint) throws Exception {
    File archiveDir = new File(archiveDirectory);

    if (!archiveDir.exists()) {
      archiveDir.mkdirs();
    }
  }

  @Transactional
  public void writeItems(List<Object> items) throws Exception {
    try (PrintWriter archive =
        new PrintWriter(
            new BufferedWriter(
                new FileWriter(
                    archiveDirectory + "/archive_" + System.currentTimeMillis() + ".csv", true)))) {

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
