package quarkus.tutorial.cart.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import quarkus.tutorial.cart.common.BookException;

import java.util.List;

@Component
public class CartServiceClient {

    @Value("${cart.service.url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final CookieFilter cookieFilter;

    public CartServiceClient() {
        this.restTemplate = new RestTemplate();
        this.cookieFilter = new CookieFilter();
        this.restTemplate.getInterceptors().add(cookieFilter);
    }

    public void initialize(String name, String id) throws BookException {
        String url = baseUrl + "/cart/initialize?name={name}";
        if (id != null) {
            url += "&id={id}";
            restTemplate.postForEntity(url, null, Void.class, name, id);
        } else {
            restTemplate.postForEntity(url, null, Void.class, name);
        }
    }

    public void addBook(String title) {
        String url = baseUrl + "/cart/add?title={title}";
        restTemplate.postForEntity(url, null, Void.class, title);
    }

    public List<String> getContents() {
        String url = baseUrl + "/cart/contents";
        ResponseEntity<List<String>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<String>>() {}
        );
        return response.getBody();
    }

    public void removeBook(String title) throws BookException {
        String url = baseUrl + "/cart/remove?title={title}";
        restTemplate.delete(url, title);
    }

    public void clearCart() {
        String url = baseUrl + "/cart/clear";
        restTemplate.postForEntity(url, null, Void.class);
    }
}
