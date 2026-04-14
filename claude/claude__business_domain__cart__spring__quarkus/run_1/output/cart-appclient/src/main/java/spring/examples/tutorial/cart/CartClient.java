package spring.examples.tutorial.cart;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.List;

@ApplicationScoped
public class CartClient {

    @Inject
    @RestClient
    CartRestClient cartRestClient;

    public void doCartOperations() {
        cartRestClient.initialize("Duke d'Url", "123");

        cartRestClient.addBook("Infinite Jest");
        cartRestClient.addBook("Bel Canto");
        cartRestClient.addBook("Kafka on the Shore");

        List<String> books = cartRestClient.getContents();

        books.forEach(title -> System.out
                .println("Retrieving book title from cart: " + title));

        System.out.println("Removing \"Gravity's Rainbow\" from cart.");
        try {
            cartRestClient.removeBook("Gravity's Rainbow");
        } catch (Exception e) {
            System.err.println("Failed to remove book: " + e.getMessage());
        }

        cartRestClient.clear();
    }

}
