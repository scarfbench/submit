package spring.examples.tutorial.cart;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Application {

    public static void main(String[] args) {
        try {
            // Load configuration
            Properties props = new Properties();
            try (InputStream input = Application.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (input != null) {
                    props.load(input);
                }
            }

            String baseUrl = props.getProperty("app.cart.url", "http://localhost:8080/cart");

            // Create HTTP client with cookie store for session management
            CookieStore cookieStore = new BasicCookieStore();
            CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .setDefaultCookieStore(cookieStore)
                    .build();

            // Create and run cart client
            CartClient cartClient = new CartClient(baseUrl, httpClient);
            cartClient.doCartOperations();

            httpClient.close();
            System.out.println("Client operations completed successfully");
        } catch (IOException ex) {
            System.err.println("Error during cart operations: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
