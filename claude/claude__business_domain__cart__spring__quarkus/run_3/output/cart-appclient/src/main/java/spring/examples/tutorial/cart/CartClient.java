package spring.examples.tutorial.cart;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.List;

@ApplicationScoped
public class CartClient {

    @ConfigProperty(name = "app.cart.url")
    String baseUrl;

    public void doCartOperations() {
        SessionAwareRestTemplate sessionTemplate = new SessionAwareRestTemplate();
        Client client = sessionTemplate.getClient();

        // Initialize cart
        Form initForm = new Form();
        initForm.param("person", "Duke d'Url");
        initForm.param("id", "123");
        client.target(baseUrl + "/initialize")
                .request()
                .post(Entity.form(initForm));

        // Add books
        Form addForm1 = new Form().param("title", "Infinite Jest");
        client.target(baseUrl + "/add")
                .request()
                .post(Entity.form(addForm1));

        Form addForm2 = new Form().param("title", "Bel Canto");
        client.target(baseUrl + "/add")
                .request()
                .post(Entity.form(addForm2));

        Form addForm3 = new Form().param("title", "Kafka on the Shore");
        client.target(baseUrl + "/add")
                .request()
                .post(Entity.form(addForm3));

        // Get contents
        List<String> books = client.target(baseUrl + "/contents")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<String>>() {});

        books.forEach(title -> System.out
                .println("Retrieving book title from cart: " + title));

        System.out.println("Removing \"Gravity's Rainbow\" from cart.");
        client.target(baseUrl + "/remove")
                .queryParam("title", "Gravity's Rainbow")
                .request()
                .delete();

        // Clear cart
        client.target(baseUrl + "/clear")
                .request()
                .post(Entity.text(""));
    }

}
