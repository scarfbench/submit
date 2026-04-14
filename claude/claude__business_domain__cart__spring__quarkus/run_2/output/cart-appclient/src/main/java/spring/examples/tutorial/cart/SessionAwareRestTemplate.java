package spring.examples.tutorial.cart;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.List;

public class SessionAwareRestTemplate {

    private HttpClient httpClient;
    private CookieStore cookieStore;
    private ObjectMapper objectMapper;

    public SessionAwareRestTemplate() {
        this.cookieStore = new BasicCookieStore();
        this.httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public void post(String url) {
        try {
            HttpPost httpPost = new HttpPost(url);
            httpClient.execute(httpPost, response -> {
                EntityUtils.consume(response.getEntity());
                return null;
            });
        } catch (IOException e) {
            throw new RuntimeException("Error executing POST request", e);
        }
    }

    public List<String> getList(String url) {
        try {
            HttpGet httpGet = new HttpGet(url);
            return httpClient.execute(httpGet, response -> {
                String json = EntityUtils.toString(response.getEntity());
                return objectMapper.readValue(json, new TypeReference<List<String>>() {});
            });
        } catch (IOException e) {
            throw new RuntimeException("Error executing GET request", e);
        }
    }

    public void delete(String url) {
        try {
            HttpDelete httpDelete = new HttpDelete(url);
            httpClient.execute(httpDelete, response -> {
                EntityUtils.consume(response.getEntity());
                return null;
            });
        } catch (IOException e) {
            throw new RuntimeException("Error executing DELETE request", e);
        }
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }
}
