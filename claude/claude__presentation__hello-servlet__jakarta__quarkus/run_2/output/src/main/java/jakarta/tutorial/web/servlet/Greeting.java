package jakarta.tutorial.web.servlet;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/greeting")
public class Greeting {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response doGet(@QueryParam("name") String name) {
        if (name == null || name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String greeting = "Hello, " + name + "!";
        return Response.ok(greeting).build();
    }
}