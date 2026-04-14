package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.File;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateConverter;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UploadDirectoryScanner {

    private static final String UPLOAD_DIRECTORY = "/tmp/uploads";

    @Autowired
    private Logger logger;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Scheduled(fixedRate = 120000) // Every 2 minutes
    public void processFiles() {
        File uploadDirectory = new File(UPLOAD_DIRECTORY);

        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
            return;
        }

        File[] files = uploadDirectory.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            try {
                processFile(file);
                file.delete();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error processing file: " + file.getName(), e);
            }
        }
    }

    private void processFile(File file) throws Exception {
        logger.log(Level.INFO, "Processing file: {0}", file.getName());
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            String line;
            while ((line = raf.readLine()) != null) {
                try {
                    HandlingEventRegistrationAttempt attempt = parseLine(line);
                    applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
                } catch (EventLineParseException e) {
                    logger.log(Level.WARNING, "Problem parsing line: " + line, e);
                }
            }
        }
    }

    private HandlingEventRegistrationAttempt parseLine(String line) throws EventLineParseException {
        String[] result = line.split(",");

        if (result.length != 5) {
            throw new EventLineParseException("Wrong number of data elements", line);
        }

        LocalDateTime completionTime;
        try {
            completionTime = DateConverter.toDateTime(result[0]);
        } catch (DateTimeParseException e) {
            throw new EventLineParseException("Cannot parse completion time", e, line);
        }

        TrackingId trackingId;
        try {
            trackingId = new TrackingId(result[1]);
        } catch (NullPointerException e) {
            throw new EventLineParseException("Cannot parse tracking ID", e, line);
        }

        VoyageNumber voyageNumber = null;
        try {
            if (!result[2].isEmpty()) {
                voyageNumber = new VoyageNumber(result[2]);
            }
        } catch (NullPointerException e) {
            throw new EventLineParseException("Cannot parse voyage number", e, line);
        }

        UnLocode unLocode;
        try {
            unLocode = new UnLocode(result[3]);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new EventLineParseException("Cannot parse UN location code", e, line);
        }

        HandlingEvent.Type eventType;
        try {
            eventType = HandlingEvent.Type.valueOf(result[4]);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new EventLineParseException("Cannot parse event type", e, line);
        }

        return new HandlingEventRegistrationAttempt(
                LocalDateTime.now(), completionTime, trackingId, voyageNumber, eventType, unLocode);
    }
}
