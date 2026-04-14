package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Adapted Task runnable. Posts updates to REST endpoint using simple client.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Task implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Task.class);
    private static final String WS_URL = "http://localhost:8080/taskinfo";

    private String name;
    private String type;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private int counter = 1;

    @Autowired
    private TaskRestPoster restPoster;

    public Task() {
        this.name = "unknown";
        this.type = "IMMEDIATE";
    }

    public void init(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void run() {
        if ("PERIODIC".equals(type))
            sendToWebService("started run #" + counter);
        else
            sendToWebService("started");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if ("PERIODIC".equals(type))
            sendToWebService("finished run #" + (counter++));
        else
            sendToWebService("finished");
    }

    private void sendToWebService(String details) {
        String time = dateFormat.format(Calendar.getInstance().getTime());
        String msg = time + " - " + type + " Task " + name + " " + details;
        try {
            restPoster.post(msg);
        } catch (Exception e) {
            log.error("Failed posting task update: {}", msg, e);
        }
    }

    public String getName() {
        return name;
    }
}
