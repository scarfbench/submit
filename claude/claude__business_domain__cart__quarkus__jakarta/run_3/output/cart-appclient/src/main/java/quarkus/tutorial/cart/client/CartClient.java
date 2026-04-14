package quarkus.tutorial.cart.client;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import quarkus.tutorial.cart.common.BookException;
import java.util.List;

public class CartClient {

    public static void main(String... args) throws Exception {
        CartClient client = new CartClient();
        int exitCode = client.run(args);
        System.exit(exitCode);
    }

    public int run(String... args) throws Exception {
        // Create REST client using JAX-RS Client API
        String baseUrl = System.getProperty("cart.service.url", "http://localhost:8080");
        WebTarget target = ClientBuilder.newClient()
                .register(CookieFilter.class)
                .target(baseUrl);

        CartServiceClient cart = WebResourceFactory.newResource(CartServiceClient.class, target);

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

        return 0;
    }

}
