package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateConverter;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EventItemWriter implements ItemWriter<HandlingEventRegistrationAttempt> {

    private static final String ARCHIVE_DIRECTORY = "archive_directory";
    private static final Logger logger = Logger.getLogger(EventItemWriter.class.getName());

    @Autowired
    private ApplicationEvents applicationEvents;

    @Override
    @Transactional
    public void write(Chunk<? extends HandlingEventRegistrationAttempt> chunk) throws Exception {
        File archiveDirectory = new File(ARCHIVE_DIRECTORY);
        if (!archiveDirectory.exists()) {
            archiveDirectory.mkdirs();
        }

        try (PrintWriter archive = new PrintWriter(
                new BufferedWriter(
                        new FileWriter(
                                archiveDirectory + "/archive_EventFilesProcessorJob_"
                                        + System.currentTimeMillis() + ".csv",
                                true)))) {
            for (HandlingEventRegistrationAttempt attempt : chunk) {
                applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
                archive.println(
                        DateConverter.toString(attempt.getRegistrationTime())
                                + "," + DateConverter.toString(attempt.getCompletionTime())
                                + "," + attempt.getTrackingId()
                                + "," + attempt.getVoyageNumber()
                                + "," + attempt.getUnLocode()
                                + "," + attempt.getType());
            }
        }
    }
}
