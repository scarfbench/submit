package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedExecutorService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@ApplicationScoped
@Path("/taskinfo")
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    @Resource
    private ManagedScheduledExecutorService scheduler;

    @Resource
    private ManagedExecutorService executor;

    private final Map<String, ScheduledFuture<?>> periodicTasks = new ConcurrentHashMap<>();
    private volatile String infoField = "";

    @Inject
    private TaskUpdateEvents events;

    @Inject
    private TaskRestPoster poster;

    public Task newTask(String name, String type) {
        return new Task(poster, name, type);
    }

    public void submitTask(Task task, String type) {
        switch (type) {
            case "IMMEDIATE" -> executor.submit(task);
            case "DELAYED"   -> scheduler.schedule(task, 3, TimeUnit.SECONDS);
            case "PERIODIC"  -> {
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

    @POST
    @Consumes({MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
    public void addToInfoField(String msg) {
        infoField = msg + "\n" + infoField;
        log.info("[TaskService] Added message {}", msg);
        events.fire("infobox");
    }

    public String getInfoField() { return infoField; }
    public void clearInfoField() { infoField = ""; }
    public Set<String> getPeriodicTasks() { return periodicTasks.keySet(); }

    @PreDestroy
    void shutdown() {
        periodicTasks.values().forEach(f -> f.cancel(true));
    }
}
