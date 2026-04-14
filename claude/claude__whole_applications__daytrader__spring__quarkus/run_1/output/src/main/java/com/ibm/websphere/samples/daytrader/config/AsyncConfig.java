package com.ibm.websphere.samples.daytrader.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@ApplicationScoped
public class AsyncConfig {

    @Produces
    @Named("ManagedExecutorService")
    @Singleton
    public ExecutorService managedExecutorService() {
        return Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setName("mes-" + t.getId());
            t.setDaemon(true);
            return t;
        });
    }

    @Produces
    @Named("ManagedScheduledTaskExecutor")
    @Singleton
    public ScheduledExecutorService taskScheduler() {
        return Executors.newScheduledThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setName("mstex-" + t.getId());
            t.setDaemon(true);
            return t;
        });
    }

    @Produces
    @Named("ManagedThreadFactory")
    @Singleton
    public ThreadFactory managedThreadFactoryBean() {
        return r -> {
            Thread t = new Thread(r);
            t.setName("mes-" + t.getId());
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((thread, e) -> {
                System.err.println("Uncaught in " + thread.getName());
                e.printStackTrace();
            });
            return t;
        };
    }
}
