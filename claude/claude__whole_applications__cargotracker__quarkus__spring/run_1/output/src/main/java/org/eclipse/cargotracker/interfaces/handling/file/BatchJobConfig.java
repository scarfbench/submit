package org.eclipse.cargotracker.interfaces.handling.file;

import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchJobConfig {

    @Bean
    public Job eventFilesProcessorJob(
            JobRepository jobRepository,
            Step eventProcessingStep,
            FileProcessorJobListener jobListener) {
        return new JobBuilder("EventFilesProcessorJob", jobRepository)
                .listener(jobListener)
                .start(eventProcessingStep)
                .build();
    }

    @Bean
    public Step eventProcessingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            EventItemReader reader,
            EventItemWriter writer,
            LineParseExceptionListener skipListener) {
        return new StepBuilder("eventProcessingStep", jobRepository)
                .<HandlingEventRegistrationAttempt, HandlingEventRegistrationAttempt>chunk(10, transactionManager)
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .skip(EventLineParseException.class)
                .skipLimit(100)
                .listener(skipListener)
                .build();
    }
}
