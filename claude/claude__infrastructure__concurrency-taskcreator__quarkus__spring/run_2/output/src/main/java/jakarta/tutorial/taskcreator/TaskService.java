package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Replacement for the original EJB. Uses standard executors managed inside a
 * service bean. Provides REST endpoint for task updates.
 */
@Service
@RestController
@RequestMapping("/taskinfo")
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final Map<String, ScheduledFuture<?>> periodicTasks = new ConcurrentHashMap<>();
    private volatile String infoField = "";

    @Autowired
    private TaskUpdateEvents events;

    public void submitTask(Task task, String type) {
        switch (type) {
            case "IMMEDIATE" -> executor.submit(task);
            case "DELAYED" -> scheduler.schedule(task, 3, TimeUnit.SECONDS);
            case "PERIODIC" -> {
                ScheduledFuture<?> fut = scheduler.scheduleAtFixedRate(task, 0, 8, TimeUnit.SECONDS);
                periodicTasks.put(task.getName(), fut);
            }
        }
    }

    public void cancelPeriodicTask(String name) {
        if (periodicTasks.containsKey(name)) {
            log.info("[TaskService] Cancelling task {}", name);
            periodicTasks.get(name).cancel(true);
            periodicTasks.remove(name);
            events.fire("tasklist");
        }
    }

    @PostMapping(consumes = {"text/html", "text/plain"})
    public void addToInfoField(@RequestBody String msg) {
        infoField = msg + "\n" + infoField;
        log.info("[TaskService] Added message {}", msg);
        events.fire("infobox");
    }

    public String getInfoField() {
        return infoField;
    }

    public void clearInfoField() {
        infoField = "";
    }

    public Set<String> getPeriodicTasks() {
        return periodicTasks.keySet();
    }

    @PreDestroy
    void shutdown() {
        periodicTasks.values().forEach(f -> f.cancel(true));
        executor.shutdownNow();
        scheduler.shutdownNow();
    }
}
