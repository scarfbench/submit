package spring.examples.tutorial.cart;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;

@Path("/cart")
@RegisterRestClient(configKey = "cart-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CartRestClient {

    @POST
    @Path("/initialize")
    void initialize(@QueryParam("person") String person, @QueryParam("id") String id);

    @POST
    @Path("/add")
    void addBook(@QueryParam("title") String title);

    @DELETE
    @Path("/remove")
    void removeBook(@QueryParam("title") String title);

    @GET
    @Path("/contents")
    List<String> getContents();

    @POST
    @Path("/clear")
    void clear();
}
