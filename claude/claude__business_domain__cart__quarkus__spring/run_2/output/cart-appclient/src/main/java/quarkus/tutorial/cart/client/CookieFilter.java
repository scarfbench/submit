package quarkus.tutorial.cart.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CookieFilter implements ClientHttpRequestInterceptor {

    private static final AtomicReference<String> sessionCookie = new AtomicReference<>();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String cookie = sessionCookie.get();
        if (cookie != null) {
            request.getHeaders().add("Cookie", cookie);
        }

        ClientHttpResponse response = execution.execute(request, body);

        List<String> setCookies = response.getHeaders().get("Set-Cookie");
        if (setCookies != null && !setCookies.isEmpty()) {
            sessionCookie.set(String.join("; ", setCookies));
        }

        return response;
    }
}
