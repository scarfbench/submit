package jakarta.tutorial.concurrency.jobs.service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
@Qualifier
@interface High {}

@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
@Qualifier
@interface Low {}

@Configuration
public class ExecutorProducers {

    // High priority: more threads, bigger queue
    @Bean
    @High
    public Executor highPriorityExecutor() {
        return Executors.newFixedThreadPool(32);
    }

    // Low priority: fewer threads
    @Bean
    @Low
    public Executor lowPriorityExecutor() {
        return Executors.newFixedThreadPool(8);
    }
}
