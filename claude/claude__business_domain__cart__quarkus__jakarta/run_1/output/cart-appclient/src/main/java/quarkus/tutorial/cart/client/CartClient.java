package quarkus.tutorial.cart.client;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.GenericType;
import quarkus.tutorial.cart.common.BookException;
import java.util.List;

public class CartClient {

    private static final String BASE_URL = "http://localhost:8080/cart-app/cart";

    public static void main(String[] args) {
        CartClient client = new CartClient();
        client.run();
    }

    public void run() {
        Client client = ClientBuilder.newClient();
        client.register(CookieFilter.class);

        try {
            // Initialize cart
            client.target(BASE_URL + "/initialize")
                    .queryParam("name", "Duke d'Url")
                    .queryParam("id", "123")
                    .request()
                    .post(Entity.json(""));

            // Add books
            client.target(BASE_URL + "/add")
                    .queryParam("title", "Infinite Jest")
                    .request()
                    .post(Entity.json(""));

            client.target(BASE_URL + "/add")
                    .queryParam("title", "Bel Canto")
                    .request()
                    .post(Entity.json(""));

            client.target(BASE_URL + "/add")
                    .queryParam("title", "Kafka on the Shore")
                    .request()
                    .post(Entity.json(""));

            // Get contents
            List<String> bookList = client.target(BASE_URL + "/contents")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<String>>() {});

            for (String title : bookList) {
                System.out.println("Retrieving book title from cart: " + title);
            }

            // Remove a book that's not in cart (will cause error)
            System.out.println("Removing \"Gravity's Rainbow\" from cart.");
            client.target(BASE_URL + "/remove")
                    .queryParam("title", "Gravity's Rainbow")
                    .request()
                    .delete();

            // Clear cart
            client.target(BASE_URL + "/clear")
                    .request()
                    .post(Entity.json(""));

        } catch (RuntimeException ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
        } finally {
            client.close();
        }
    }
}
