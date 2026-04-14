package spring.tutorial.web.dukeetf;

import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PriceVolumeService {
    private static final Logger log = Logger.getLogger("PriceVolumeService");

    private final Queue<CompletableFuture<ResponseEntity<String>>> queue = new ConcurrentLinkedQueue<>();
    private final Random random = new Random();

    private volatile double price = 100.0;
    private volatile int volume = 300000;

    @PostConstruct
    void init() {
        log.log(Level.INFO, "Initializing scheduler-backed service.");
    }

    public void register(CompletableFuture<ResponseEntity<String>> future) {
        queue.add(future);
        log.log(Level.INFO, "Connection open (queued).");
    }

    @Scheduled(fixedRate = 1000)
    void tick() {
        price += 1.0 * (random.nextInt(100) - 50) / 100.0;
        volume += random.nextInt(5000) - 2500;
        flush();
    }


    private void flush() {
        String msg = String.format("%.2f / %d", price, volume);
        for (CompletableFuture<ResponseEntity<String>> future; (future = queue.poll()) != null; ) {
            try {
                future.complete(ResponseEntity.ok().header("Content-Type", "text/html").body(msg));
                log.log(Level.INFO, "Sent: {0}", msg);
            } catch (Exception e) {
                log.log(Level.INFO, "Send failed: {0}", e.toString());
            }
        }
    }
}
