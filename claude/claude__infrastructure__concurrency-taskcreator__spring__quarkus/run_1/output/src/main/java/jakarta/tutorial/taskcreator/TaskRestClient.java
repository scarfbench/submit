package jakarta.tutorial.taskcreator;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/taskinfo")
@RegisterRestClient(configKey = "jakarta.tutorial.taskcreator.TaskRestClient")
public interface TaskRestClient {

    @POST
    @jakarta.ws.rs.Consumes(MediaType.TEXT_PLAIN)
    void postTaskInfo(String message);
}
