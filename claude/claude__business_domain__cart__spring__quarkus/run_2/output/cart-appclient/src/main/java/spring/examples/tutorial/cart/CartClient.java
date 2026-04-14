package spring.examples.tutorial.cart;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.List;

@ApplicationScoped
public class CartClient {

    @ConfigProperty(name = "app.cart.url")
    String baseUrl;

    public void doCartOperations() {
        SessionAwareRestTemplate sessionTemplate = new SessionAwareRestTemplate();

        sessionTemplate.post(baseUrl + "/initialize?person=Duke d'Url&id=123");

        sessionTemplate.post(baseUrl + "/add?title=Infinite Jest");
        sessionTemplate.post(baseUrl + "/add?title=Bel Canto");
        sessionTemplate.post(baseUrl + "/add?title=Kafka on the Shore");

        List<String> books = sessionTemplate.getList(baseUrl + "/contents");

        books.forEach(title -> System.out
                .println("Retrieving book title from cart: " + title));

        System.out.println("Removing \"Gravity's Rainbow\" from cart.");
        sessionTemplate.delete(baseUrl + "/remove?title=Gravity's Rainbow");

        sessionTemplate.post(baseUrl + "/clear");
    }

}
