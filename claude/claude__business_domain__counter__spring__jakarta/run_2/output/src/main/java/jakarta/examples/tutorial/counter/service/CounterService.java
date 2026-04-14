package jakarta.examples.tutorial.counter.service;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Counter service using Jakarta CDI
 * ApplicationScoped ensures single instance across the application
 */
@ApplicationScoped
public class CounterService {
    private int hits = 1;

    public int getHits() {
        return hits++;
    }
}
