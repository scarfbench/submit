package quarkus.tutorial.cart.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import quarkus.tutorial.cart.common.BookException;

import java.util.List;

@Component
public class CartServiceClient {

    @Value("${cart.service.url}")
    private String serviceUrl;

    private final RestTemplate restTemplate;
    private final CookieFilter cookieFilter;

    public CartServiceClient() {
        this.restTemplate = new RestTemplate();
        this.cookieFilter = new CookieFilter();
        this.restTemplate.getInterceptors().add(cookieFilter);
    }

    public void initialize(String name, String id) throws BookException {
        String url = serviceUrl + "/cart/initialize?name={name}&id={id}";
        try {
            restTemplate.postForEntity(url, null, Void.class, name, id);
        } catch (Exception e) {
            throw new BookException("Failed to initialize cart: " + e.getMessage());
        }
    }

    public void addBook(String title) {
        String url = serviceUrl + "/cart/add?title={title}";
        restTemplate.postForEntity(url, null, Void.class, title);
    }

    public List<String> getContents() {
        String url = serviceUrl + "/cart/contents";
        ResponseEntity<List<String>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<String>>() {}
        );
        return response.getBody();
    }

    public void removeBook(String title) throws BookException {
        String url = serviceUrl + "/cart/remove?title={title}";
        try {
            restTemplate.delete(url, title);
        } catch (Exception e) {
            throw new BookException("Failed to remove book: " + e.getMessage());
        }
    }

    public void clearCart() {
        String url = serviceUrl + "/cart/clear";
        restTemplate.postForEntity(url, null, Void.class);
    }
}
