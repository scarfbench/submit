package spring.examples.tutorial.cart;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

public class SessionAwareRestTemplate {

    private Client client;
    private CookieStore cookieStore;

    public SessionAwareRestTemplate() {
        this.cookieStore = new BasicCookieStore();

        org.apache.hc.client5.http.classic.HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .build();

        this.client = ClientBuilder.newClient();
    }

    public Client getClient() {
        return client;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }
}
