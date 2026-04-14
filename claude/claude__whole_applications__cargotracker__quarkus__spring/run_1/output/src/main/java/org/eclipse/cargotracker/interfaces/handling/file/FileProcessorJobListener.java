package org.eclipse.cargotracker.interfaces.handling.file;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class FileProcessorJobListener implements JobExecutionListener {

    private static final Logger logger = Logger.getLogger(FileProcessorJobListener.class.getName());

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.log(Level.INFO, "Handling event file processor batch job starting at {0}", new Date());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        logger.log(Level.INFO, "Handling event file processor batch job completed at {0}", new Date());
    }
}
