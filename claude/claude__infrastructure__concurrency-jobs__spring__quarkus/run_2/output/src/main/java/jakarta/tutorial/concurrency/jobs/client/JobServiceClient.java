package jakarta.tutorial.concurrency.jobs.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "job-service-api")
@Path("/JobService")
public interface JobServiceClient {

    @POST
    @Path("/process")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response processJob(@QueryParam("jobID") int jobID, @HeaderParam("X-REST-API-Key") String apiKey);
}