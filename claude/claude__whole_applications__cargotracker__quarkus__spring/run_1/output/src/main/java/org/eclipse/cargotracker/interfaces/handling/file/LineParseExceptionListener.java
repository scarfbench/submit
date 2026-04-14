package org.eclipse.cargotracker.interfaces.handling.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class LineParseExceptionListener implements SkipListener<HandlingEventRegistrationAttempt, HandlingEventRegistrationAttempt> {

    private static final String FAILED_DIRECTORY = "failed_directory";
    private static final Logger logger = Logger.getLogger(LineParseExceptionListener.class.getName());

    @Override
    public void onSkipInRead(Throwable t) {
        if (t instanceof EventLineParseException) {
            EventLineParseException parseException = (EventLineParseException) t;
            logger.log(Level.WARNING, "Problem parsing event file line", parseException);

            File failedDirectory = new File(FAILED_DIRECTORY);
            if (!failedDirectory.exists()) {
                failedDirectory.mkdirs();
            }

            try (PrintWriter failed = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(
                                    new File(failedDirectory, "failed_" + System.currentTimeMillis() + ".csv"),
                                    true)))) {
                failed.println(parseException.getLine());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error writing to failed file", e);
            }
        }
    }

    @Override
    public void onSkipInWrite(HandlingEventRegistrationAttempt item, Throwable t) {
        // No-op
    }

    @Override
    public void onSkipInProcess(HandlingEventRegistrationAttempt item, Throwable t) {
        // No-op
    }
}
