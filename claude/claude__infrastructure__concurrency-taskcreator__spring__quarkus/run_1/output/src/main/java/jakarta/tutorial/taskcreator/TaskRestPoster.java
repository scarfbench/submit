package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class TaskRestPoster {
    private static final Logger log = LoggerFactory.getLogger(TaskRestPoster.class);

    @Inject
    @RestClient
    TaskRestClient client;

    public void post(String msg) {
        try {
            client.postTaskInfo(msg);
        } catch (WebApplicationException e) {
            log.error("Failed posting task update ({}): {}", e.getResponse().getStatus(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed posting task update", e);
        }
    }
}