package org.eclipse.cargotracker.interfaces.handling.file;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations from the contents by calling a batch job.
 */
@Component
public class UploadDirectoryScanner {

    private static final Logger logger = Logger.getLogger(UploadDirectoryScanner.class.getName());

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job eventFilesProcessorJob;

    @Scheduled(fixedRate = 120000) // Every 2 minutes
    public void processFiles() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(eventFilesProcessorJob, params);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error running event file processor job", e);
        }
    }
}
