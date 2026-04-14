package quarkus.tutorial.counter.ejb;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CounterBean {
    private int hits = 1;

    // Increment and return the number of hits
    public int getHits() {
        return hits++;
    }
}
