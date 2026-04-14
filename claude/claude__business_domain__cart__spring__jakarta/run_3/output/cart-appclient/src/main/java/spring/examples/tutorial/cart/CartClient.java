package spring.examples.tutorial.cart;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;

import java.util.List;

public class CartClient {

    private final String baseUrl;

    public CartClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void doCartOperations() {
        // Create a cookie store for session management
        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .build();

        // Configure Jersey client with Apache HttpClient
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new Apache5ConnectorProvider());
        clientConfig.property("jersey.config.apache5.client.httpClient", httpClient);

        Client client = ClientBuilder.newClient(clientConfig);
        WebTarget target = client.target(baseUrl);

        // Initialize cart
        Form initForm = new Form();
        initForm.param("person", "Duke d'Url");
        initForm.param("id", "123");
        target.path("/initialize")
                .request()
                .post(Entity.form(initForm));

        // Add books
        Form addForm1 = new Form();
        addForm1.param("title", "Infinite Jest");
        target.path("/add")
                .request()
                .post(Entity.form(addForm1));

        Form addForm2 = new Form();
        addForm2.param("title", "Bel Canto");
        target.path("/add")
                .request()
                .post(Entity.form(addForm2));

        Form addForm3 = new Form();
        addForm3.param("title", "Kafka on the Shore");
        target.path("/add")
                .request()
                .post(Entity.form(addForm3));

        // Get contents
        List<String> books = target.path("/contents")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<String>>() {});

        books.forEach(title -> System.out
                .println("Retrieving book title from cart: " + title));

        // Remove book
        System.out.println("Removing \"Gravity's Rainbow\" from cart.");
        target.path("/remove")
                .queryParam("title", "Gravity's Rainbow")
                .request()
                .delete();

        // Clear cart
        target.path("/clear")
                .request()
                .post(Entity.text(""));

        client.close();
    }

}
