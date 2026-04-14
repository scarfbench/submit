package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@ApplicationScoped
public class TaskRestPoster {
    private static final Logger log = LoggerFactory.getLogger(TaskRestPoster.class);
    private Client client;
    private String baseUrl = "http://localhost:9080/taskcreator";

    @PostConstruct
    public void init() {
        this.client = ClientBuilder.newClient();
    }

    @PreDestroy
    public void cleanup() {
        if (client != null) {
            client.close();
        }
    }

    public void post(String msg) {
        try {
            Response response = client.target(baseUrl)
                    .path("/taskinfo")
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
