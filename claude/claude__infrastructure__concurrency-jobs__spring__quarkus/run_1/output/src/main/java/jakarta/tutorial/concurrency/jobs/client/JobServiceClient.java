package jakarta.tutorial.concurrency.jobs.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/webapi")
@RegisterRestClient(configKey = "jobs-service")
public interface JobServiceClient {

    @POST
    @Path("/JobService/process")
    @Produces("text/plain")
    Response processJob(@QueryParam("jobID") int jobID,
                        @HeaderParam("X-REST-API-Key") String apiKey);
}