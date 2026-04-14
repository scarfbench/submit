package jakarta.tutorial.concurrency.jobs.service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Qualifier;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier @Retention(RUNTIME) @Target({TYPE, METHOD, FIELD, PARAMETER})
@interface High {}
@Qualifier @Retention(RUNTIME) @Target({TYPE, METHOD, FIELD, PARAMETER})
@interface Low {}

@ManagedExecutorDefinition(
    name = "java:comp/concurrent/HighPriorityExecutor",
    maxAsync = 32
)
@ManagedExecutorDefinition(
    name = "java:comp/concurrent/LowPriorityExecutor",
    maxAsync = 8
)
@ApplicationScoped
public class ExecutorProducers {

    @Resource(lookup = "java:comp/concurrent/HighPriorityExecutor")
    private ManagedExecutorService highExecutor;

    @Resource(lookup = "java:comp/concurrent/LowPriorityExecutor")
    private ManagedExecutorService lowExecutor;

    @Produces @ApplicationScoped @High
    public ManagedExecutorService high() {
        return highExecutor;
    }

    @Produces @ApplicationScoped @Low
    public ManagedExecutorService low() {
        return lowExecutor;
    }
}