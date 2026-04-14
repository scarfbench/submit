package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateConverter;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

@ApplicationScoped
public class EventFileProcessor {

    private static final String UPLOAD_DIRECTORY = "/tmp/cargo-tracker/upload";
    private static final String ARCHIVE_DIRECTORY = "/tmp/cargo-tracker/archive";

    @Inject
    Logger logger;

    @Inject
    ApplicationEvents applicationEvents;

    @Transactional
    public void processEventFiles() {
        File uploadDir = new File(UPLOAD_DIRECTORY);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            return;
        }

        File[] files = uploadDir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        File archiveDir = new File(ARCHIVE_DIRECTORY);
        if (!archiveDir.exists()) {
            archiveDir.mkdirs();
        }

        for (File file : files) {
            processFile(file, archiveDir);
        }
    }

    private void processFile(File file, File archiveDir) {
        logger.log(Level.INFO, "Processing event file: {0}", file.getName());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    processLine(line);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error processing line: " + line, e);
                }
            }
            // Move to archive
            file.renameTo(new File(archiveDir, file.getName()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing file: " + file.getName(), e);
        }
    }

    private void processLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 5) {
            throw new RuntimeException("Wrong number of data elements in line: " + line);
        }

        LocalDateTime completionTime = DateConverter.toDateTime(parts[0]);
        TrackingId trackingId = new TrackingId(parts[1]);
        VoyageNumber voyageNumber = parts[2].isEmpty() ? null : new VoyageNumber(parts[2]);
        UnLocode unLocode = new UnLocode(parts[3]);
        HandlingEvent.Type eventType = HandlingEvent.Type.valueOf(parts[4]);

        HandlingEventRegistrationAttempt attempt = new HandlingEventRegistrationAttempt(
            LocalDateTime.now(), completionTime, trackingId, voyageNumber, eventType, unLocode);

        applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
    }
}
