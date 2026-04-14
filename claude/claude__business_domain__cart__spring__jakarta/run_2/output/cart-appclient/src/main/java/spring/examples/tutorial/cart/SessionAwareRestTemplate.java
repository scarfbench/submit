package spring.examples.tutorial.cart;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class SessionAwareRestTemplate {

    private RestTemplate restTemplate;
    private CookieStore cookieStore;

    public SessionAwareRestTemplate() {
        this.cookieStore = new BasicCookieStore();

        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        this.restTemplate = new RestTemplate(factory);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }
}
