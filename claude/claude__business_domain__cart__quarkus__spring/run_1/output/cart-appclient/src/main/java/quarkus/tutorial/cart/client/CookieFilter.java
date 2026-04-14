package quarkus.tutorial.cart.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.Decoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class CookieFilter {

    private static final AtomicReference<String> sessionCookie = new AtomicReference<>();

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String cookie = sessionCookie.get();
                if (cookie != null) {
                    template.header("Cookie", cookie);
                }
            }
        };
    }

    @Bean
    public Decoder feignDecoder(Decoder defaultDecoder) {
        return new Decoder() {
            @Override
            public Object decode(Response response, Type type) throws IOException {
                Collection<String> setCookies = response.headers().get("Set-Cookie");
                if (setCookies != null && !setCookies.isEmpty()) {
                    sessionCookie.set(String.join("; ", setCookies));
                }
                return defaultDecoder.decode(response, type);
            }
        };
    }
}
