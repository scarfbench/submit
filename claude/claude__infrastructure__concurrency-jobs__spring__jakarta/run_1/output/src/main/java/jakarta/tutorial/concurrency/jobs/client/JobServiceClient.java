package jakarta.tutorial.concurrency.jobs.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class JobServiceClient {

    private final Client client;
    private final String baseUrl = "http://localhost:9080/jobs/webapi";

    public JobServiceClient() {
        this.client = ClientBuilder.newClient();
    }

    public Response processJob(int jobID, String apiKey) {
        return client.target(baseUrl)
                .path("/JobService/process")
                .queryParam("jobID", jobID)
                .request(MediaType.TEXT_PLAIN)
                .header("X-REST-API-Key", apiKey)
                .post(Entity.text(""));
    }
}