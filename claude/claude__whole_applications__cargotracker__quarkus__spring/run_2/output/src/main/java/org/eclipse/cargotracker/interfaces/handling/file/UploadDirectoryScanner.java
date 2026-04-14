package org.eclipse.cargotracker.interfaces.handling.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations from the contents by calling a batch job.
 *
 * <p>
 * Files that fail to parse are moved into a separate directory, successful files are deleted.
 */
@Component
@Transactional
public class UploadDirectoryScanner {

  @Autowired
  private EventItemReader eventItemReader;

  @Autowired
  private EventItemWriter eventItemWriter;

  @Scheduled(fixedDelay = 120000) // Run every 2 minutes (120000 ms)
  public void processFiles() {
    try {
      // Simple batch processing - in production, consider using Spring Batch
      eventItemReader.open(null);

      Object item;
      java.util.List<Object> items = new java.util.ArrayList<>();

      while ((item = eventItemReader.readItem()) != null) {
        items.add(item);
      }

      if (!items.isEmpty()) {
        eventItemWriter.writeItems(items);
      }
    } catch (Exception e) {
      // Log error - batch job failed
      e.printStackTrace();
    }
  }
}
