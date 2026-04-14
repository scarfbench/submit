package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.File;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.util.DateConverter;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
public class EventItemReader implements ItemReader<HandlingEventRegistrationAttempt> {

    private static final String UPLOAD_DIRECTORY = "upload_directory";
    private static final Logger logger = Logger.getLogger(EventItemReader.class.getName());

    private List<File> files = new LinkedList<>();
    private int fileIndex = 0;
    private RandomAccessFile currentFile;
    private boolean initialized = false;

    public void initialize() {
        File uploadDirectory = new File(UPLOAD_DIRECTORY);
        logger.log(Level.INFO, "Scanning upload directory: {0}", uploadDirectory);

        if (!uploadDirectory.exists()) {
            logger.log(Level.INFO, "Upload directory does not exist, creating it");
            uploadDirectory.mkdirs();
        } else {
            File[] found = uploadDirectory.listFiles();
            if (found != null) {
                files = new LinkedList<>(Arrays.asList(found));
            }
        }

        openNextFile();
        initialized = true;
    }

    private void openNextFile() {
        try {
            if (currentFile != null) {
                currentFile.close();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error closing file", e);
        }

        currentFile = null;
        while (fileIndex < files.size()) {
            File file = files.get(fileIndex);
            try {
                currentFile = new RandomAccessFile(file, "r");
                logger.log(Level.INFO, "Processing file: {0}", file);
                return;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Cannot open file: " + file, e);
                fileIndex++;
            }
        }
    }

    @Override
    public HandlingEventRegistrationAttempt read() throws Exception {
        if (!initialized) {
            initialize();
        }

        while (currentFile != null) {
            String line = currentFile.readLine();
            if (line != null) {
                try {
                    return parseLine(line);
                } catch (EventLineParseException e) {
                    logger.log(Level.WARNING, "Error parsing line: " + line, e);
                    continue;
                }
            } else {
                // Done with current file, delete it and move to next
                logger.log(Level.INFO, "Finished processing file, deleting: {0}", files.get(fileIndex));
                currentFile.close();
                files.get(fileIndex).delete();
                fileIndex++;
                if (fileIndex < files.size()) {
                    openNextFile();
                } else {
                    currentFile = null;
                }
            }
        }

        // Reset for next run
        initialized = false;
        fileIndex = 0;
        files.clear();
        return null;
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
