package jakarta.tutorial.concurrency.jobs.exec;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.util.concurrent.*;

@ApplicationScoped
public class ExecutorConfig {

    @Produces
    @High
    @ApplicationScoped
    public ThreadPoolExecutor highExecutor() {
        int cores = Math.max(4, Runtime.getRuntime().availableProcessors());
        return new ThreadPoolExecutor(
                cores, cores * 2, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10_000),
                new ThreadFactoryBuilder("high"));
    }

    @Produces
    @Low
    @ApplicationScoped
    public ThreadPoolExecutor lowExecutor() {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        return new ThreadPoolExecutor(
                cores, cores, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2_000),
                new ThreadFactoryBuilder("low"));
    }

    static class ThreadFactoryBuilder implements ThreadFactory {
        private final String pool;
        private final ThreadFactory delegate = Executors.defaultThreadFactory();
        ThreadFactoryBuilder(String pool) { this.pool = pool; }
        @Override
        public Thread newThread(Runnable r) {
            Thread t = delegate.newThread(r);
            t.setName("jobs-" + pool + "-" + t.getId());
            t.setDaemon(false);
            return t;
        }
    }
}