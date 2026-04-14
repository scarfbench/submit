package jakarta.tutorial.concurrency.jobs.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/JobService")
@RegisterRestClient(configKey = "job-service")
public interface JobServiceClient {

    @POST
    @Path("/process")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    Response processJob(@QueryParam("jobID") int jobID, @HeaderParam("X-REST-API-Key") String apiKey);
}