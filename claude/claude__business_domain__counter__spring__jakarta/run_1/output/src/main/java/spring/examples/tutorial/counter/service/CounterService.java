package spring.examples.tutorial.counter.service;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
public class CounterService {
    private int hits = 1;

    public int getHits() {
        return hits++;
    }
}
