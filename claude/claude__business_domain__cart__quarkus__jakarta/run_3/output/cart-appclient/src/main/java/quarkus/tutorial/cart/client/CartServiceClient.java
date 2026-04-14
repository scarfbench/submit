package quarkus.tutorial.cart.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import quarkus.tutorial.cart.common.BookException;

import java.util.List;

@Path("/cart")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CartServiceClient {

    @POST
    @Path("/initialize")
    void initialize(@QueryParam("name") String name, @QueryParam("id") String id)
            throws BookException;

    @POST
    @Path("/add")
    void addBook(@QueryParam("title") String title);

    @GET
    @Path("/contents")
    List<String> getContents();

    @DELETE
    @Path("/remove")
    void removeBook(@QueryParam("title") String title) throws BookException;

    @POST
    @Path("/clear")
    void clearCart();
}
