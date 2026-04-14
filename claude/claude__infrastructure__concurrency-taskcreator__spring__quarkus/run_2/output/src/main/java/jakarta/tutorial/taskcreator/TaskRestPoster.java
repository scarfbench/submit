package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class TaskRestPoster {
    private static final Logger log = LoggerFactory.getLogger(TaskRestPoster.class);
    private final Client client;

    @Inject
    public TaskRestPoster(Client client) {
        this.client = client;
    }

    public void post(String msg) {
        try {
            Response response = client.target("http://localhost:9080")
                    .path("/taskcreator/taskinfo")
                    .request()
                    .post(Entity.entity(msg, MediaType.TEXT_PLAIN));
            int status = response.getStatus();
            if (status >= 300) {
                log.warn("Non-success response posting task update: {}", status);
            }
            response.close();
        } catch (Exception e) {
            log.error("Failed posting task update", e);
        }
    }
}