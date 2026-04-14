package quarkus.tutorial.cart.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.HttpClientErrorException;
import quarkus.tutorial.cart.common.BookException;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {"quarkus.tutorial.cart.client"})
public class CartClient implements CommandLineRunner {

    @Autowired
    CartServiceClient cart;

    public static void main(String[] args) {
        SpringApplication.run(CartClient.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
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

        } catch (HttpClientErrorException | BookException ex) {
            System.err.println("Caught a BookException: " + ex.getMessage());
        }
    }

}
