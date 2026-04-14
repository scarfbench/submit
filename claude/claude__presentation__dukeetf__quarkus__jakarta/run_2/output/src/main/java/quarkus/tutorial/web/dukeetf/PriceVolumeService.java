package quarkus.tutorial.web.dukeetf;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.io.PrintWriter;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
public class PriceVolumeService {
    private static final Logger log = Logger.getLogger("PriceVolumeService");

    private final Queue<CompletableFuture<Response>> queue = new ConcurrentLinkedQueue<>();
    private final Random random = new Random();

    private volatile double price = 100.0;
    private volatile int volume = 300000;

    private ScheduledExecutorService scheduler;

    @PostConstruct
    void init() {
        log.log(Level.INFO, "Initializing scheduler-backed service.");
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    @PreDestroy
    void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    public void register(CompletableFuture<Response> future) {
        queue.add(future);
        log.log(Level.INFO, "Connection open (queued).");
    }

    void tick() {
        price += 1.0 * (random.nextInt(100) - 50) / 100.0;
        volume += random.nextInt(5000) - 2500;
        flush();
    }


    private void flush() {
        String msg = String.format("%.2f / %d", price, volume);
        for (CompletableFuture<Response> future; (future = queue.poll()) != null; ) {
            try {
                future.complete(Response.ok(msg).type("text/html").build());
                log.log(Level.INFO, "Sent: {0}", msg);
            } catch (Exception e) {
                log.log(Level.INFO, "Send failed: {0}", e.toString());
            }
        }
    }

}
