package spring.tutorial.web.servlet;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/greeting")
public class Greeting {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String greet(@QueryParam("name") String name) {
        return "Hello, " + name + "!";
    }
}
