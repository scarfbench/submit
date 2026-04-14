package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class TaskRestPoster {
    private static final Logger log = LoggerFactory.getLogger(TaskRestPoster.class);
    private final Client client;
    private static final String BASE_URL = "http://localhost:9080";

    public TaskRestPoster() {
        this.client = ClientBuilder.newClient();
    }

    public void post(String msg) {
        try {
            Response response = client.target(BASE_URL)
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