package quarkus.tutorial.cart.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import quarkus.tutorial.cart.common.BookException;

import java.util.List;

public class CartServiceClientImpl implements CartServiceClient {

    private final WebTarget target;
    private final CookieFilter cookieFilter;

    public CartServiceClientImpl(String baseUrl) {
        cookieFilter = new CookieFilter();
        Client client = ClientBuilder.newClient()
                .register(cookieFilter);
        this.target = client.target(baseUrl).path("/cart");
    }

    @Override
    public void initialize(String name, String id) throws BookException {
        try {
            target.path("/initialize")
                    .queryParam("name", name)
                    .queryParam("id", id)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(""));
        } catch (Exception e) {
            throw new BookException("Failed to initialize cart: " + e.getMessage());
        }
    }

    @Override
    public void addBook(String title) {
        target.path("/add")
                .queryParam("title", title)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));
    }

    @Override
    public List<String> getContents() {
        return target.path("/contents")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<String>>() {});
    }

    @Override
    public void removeBook(String title) throws BookException {
        try {
            target.path("/remove")
                    .queryParam("title", title)
                    .request(MediaType.APPLICATION_JSON)
                    .delete();
        } catch (Exception e) {
            throw new BookException("Failed to remove book: " + e.getMessage());
        }
    }

    @Override
    public void clearCart() {
        target.path("/clear")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));
    }
}
