package jakarta.tutorial.taskcreator.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ApplicationScoped
public class ExecutorsConfig {

    @Produces
    @Named("taskExecutor")
    @Singleton
    public ExecutorService taskExecutor() {
        return Executors.newFixedThreadPool(4);
    }

    @Produces
    @Named("taskScheduler")
    @Singleton
    public ScheduledExecutorService taskScheduler() {
        return Executors.newScheduledThreadPool(4);
    }
}