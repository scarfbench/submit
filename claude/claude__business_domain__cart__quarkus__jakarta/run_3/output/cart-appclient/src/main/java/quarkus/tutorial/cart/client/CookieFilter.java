package quarkus.tutorial.cart.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Provider
public class CookieFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final AtomicReference<String> sessionCookie = new AtomicReference<>();

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String cookie = sessionCookie.get();
        if (cookie != null) {
            requestContext.getHeaders().add("Cookie", cookie);
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
            throws IOException {
        List<String> setCookies = responseContext.getHeaders().get("Set-Cookie");
        if (setCookies != null && !setCookies.isEmpty()) {
            // You can parse for JSESSIONID or store all cookies
            sessionCookie.set(String.join("; ", setCookies));
        }
    }
}
