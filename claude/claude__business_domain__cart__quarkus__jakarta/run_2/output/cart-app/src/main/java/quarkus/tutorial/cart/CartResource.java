package quarkus.tutorial.cart;

import java.util.List;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import quarkus.tutorial.cart.common.BookException;
import quarkus.tutorial.cart.common.Cart;

@Path("/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class CartResource {

    @Inject
    Cart cart;

    @POST
    @Path("/initialize")
    public void initialize(@QueryParam("name") String name, @QueryParam("id") String id)
            throws BookException {
        if (id == null) {
            cart.initialize(name);
        } else {
            cart.initialize(name, id);
        }
    }

    @POST
    @Path("/add")
    public void add(@QueryParam("title") String title) {
        cart.addBook(title);
    }

    @GET
    @Path("/contents")
    public List<String> getContents() {
        return cart.getContents();
    }

    @DELETE
    @Path("/remove")
    public void remove(@QueryParam("title") String title) throws BookException {
        cart.removeBook(title);
    }

    @POST
    @Path("/clear")
    public void remove(@Context HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        cart.remove();
    }
}
