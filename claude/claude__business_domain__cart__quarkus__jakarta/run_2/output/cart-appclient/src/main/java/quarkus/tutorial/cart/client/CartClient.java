package quarkus.tutorial.cart.client;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import quarkus.tutorial.cart.common.BookException;
import java.util.List;

public class CartClient {

    @Inject
    CartServiceClient cart;

    public static void main(String[] args) {
        CartClient client = new CartClient();

        // Create the REST client manually
        client.cart = new CartServiceClientImpl("http://localhost:8080");

        client.run();
    }

    public void run() {
        try {
            cart.initialize("Duke d'Url", "123");
            cart.addBook("Infinite Jest");
            cart.addBook("Bel Canto");
            cart.addBook("Kafka on the Shore");

            List<String> bookList = cart.getContents();

            for (String title : bookList) {
                System.out.println("Retrieving book title from cart: " + title);
            }

            System.out.println("Removing \"Gravity's Rainbow\" from cart.");
            cart.removeBook("Gravity's Rainbow");

            cart.clearCart();

        } catch (WebApplicationException | BookException ex) {
            System.err.println("Caught a BookException: " + ex.getMessage());
        }
    }

}
