package spring.examples.tutorial.cart;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import spring.examples.tutorial.cart.common.BookException;
import spring.examples.tutorial.cart.common.Cart;
import java.util.List;

@Path("/cart")
public class CartController {

    @Inject
    private Cart cart;

    @POST
    @Path("/initialize")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void initialize(@FormParam("person") String person,
                          @FormParam("id") String id,
                          @Context HttpServletRequest request)
            throws BookException {
        if (id == null) {
            cart.initialize(person);
        } else {
            cart.initialize(person, id);
        }
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void addBook(@FormParam("title") String title) {
        cart.addBook(title);
    }

    @DELETE
    @Path("/remove")
    public void removeBook(@QueryParam("title") String title) throws BookException {
        cart.removeBook(title);
    }

    @GET
    @Path("/contents")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getContents() {
        return cart.getContents();
    }

    @POST
    @Path("/clear")
    public void checkout(@Context HttpServletRequest request) {
        cart.remove();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

}
