package quarkus.tutorial.counter.ejb;

import org.springframework.stereotype.Component;

@Component
public class CounterBean {
    private int hits = 1;

    // Increment and return the number of hits
    public int getHits() {
        return hits++;
    }
}
