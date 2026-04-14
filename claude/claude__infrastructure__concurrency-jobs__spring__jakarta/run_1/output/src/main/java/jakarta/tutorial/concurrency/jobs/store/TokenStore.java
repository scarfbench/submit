package jakarta.tutorial.concurrency.jobs.store;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TokenStore {
    private final Set<String> store = ConcurrentHashMap.newKeySet();

    public void put(String key) {
        store.add(key);
    }
    public boolean isValid(String key) {
        return store.contains(key);
    }
}