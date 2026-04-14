package jakarta.examples.tutorial.helloservice.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.examples.tutorial.helloservice.service.HelloService;

// the original is a SOAP web service, so not exactly the same thing
@Path("/helloservice/hello")
public class HelloController {

    @Inject
    private HelloService helloService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello(@QueryParam("name") String name) {
        return helloService.sayHello(name);
    }
}
