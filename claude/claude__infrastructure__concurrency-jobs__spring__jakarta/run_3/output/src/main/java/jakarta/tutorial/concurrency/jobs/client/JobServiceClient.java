package jakarta.tutorial.concurrency.jobs.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class JobServiceClient {

    private final Client client;
    private final String baseUrl;

    @Inject
    public JobServiceClient(@ConfigProperty(name = "jobs.service.url",
                                            defaultValue = "http://localhost:9080/jobs/webapi") String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = ClientBuilder.newClient();
    }

    public JobServiceClient() {
        this.baseUrl = "http://localhost:9080/jobs/webapi";
        this.client = ClientBuilder.newClient();
    }

    public Response processJob(int jobID, String apiKey) {
        WebTarget target = client.target(baseUrl)
                .path("/JobService/process")
                .queryParam("jobID", jobID);

        return target.request()
                .header("X-REST-API-Key", apiKey)
                .post(null);
    }
}