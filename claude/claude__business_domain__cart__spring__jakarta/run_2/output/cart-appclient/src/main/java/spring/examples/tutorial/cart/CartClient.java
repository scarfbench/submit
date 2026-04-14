package spring.examples.tutorial.cart;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import java.util.List;

@ApplicationScoped
public class CartClient {

    private final String baseUrl = "http://localhost:8080/cart";

    public void doCartOperations() {
        // Use session-aware REST template
        CookieStore cookieStore = new BasicCookieStore();
        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);

        restTemplate.postForEntity(baseUrl + "/initialize?person={person}&id={id}", null,
                Void.class, "Duke d'Url", "123");

        restTemplate.postForEntity(baseUrl + "/add?title={title}", null, Void.class,
                "Infinite Jest");
        restTemplate.postForEntity(baseUrl + "/add?title={title}", null, Void.class, "Bel Canto");
        restTemplate.postForEntity(baseUrl + "/add?title={title}", null, Void.class,
                "Kafka on the Shore");

        List<String> books = restTemplate.exchange(
                baseUrl + "/contents",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {}).getBody();

        books.forEach(title -> System.out
                .println("Retrieving book title from cart: " + title));

        System.out.println("Removing \"Gravity's Rainbow\" from cart.");
        restTemplate.delete(baseUrl + "/remove?title={title}", "Gravity's Rainbow");

        restTemplate.postForEntity(baseUrl + "/clear", null, Void.class);
    }

}
