package spring.examples.tutorial.cart;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CartClient {

    private final String baseUrl;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CartClient(String baseUrl, CloseableHttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public void doCartOperations() throws IOException {
        // Initialize cart
        List<NameValuePair> initParams = new ArrayList<>();
        initParams.add(new BasicNameValuePair("person", "Duke d'Url"));
        initParams.add(new BasicNameValuePair("id", "123"));
        HttpPost initPost = new HttpPost(baseUrl + "/initialize");
        initPost.setEntity(new UrlEncodedFormEntity(initParams, StandardCharsets.UTF_8));
        httpClient.execute(initPost, response -> {
            System.out.println("Initialize response: " + response.getCode());
            return null;
        });

        // Add books
        addBook("Infinite Jest");
        addBook("Bel Canto");
        addBook("Kafka on the Shore");

        // Get contents
        HttpGet getContents = new HttpGet(baseUrl + "/contents");
        try (CloseableHttpResponse response = httpClient.execute(getContents)) {
            String json = new String(response.getEntity().getContent().readAllBytes());
            List<String> books = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            books.forEach(title -> System.out.println("Retrieving book title from cart: " + title));
        }

        // Remove book
        System.out.println("Removing \"Gravity's Rainbow\" from cart.");
        String encodedTitle = URLEncoder.encode("Gravity's Rainbow", StandardCharsets.UTF_8);
        HttpDelete deleteRequest = new HttpDelete(baseUrl + "/remove?title=" + encodedTitle);
        httpClient.execute(deleteRequest, response -> {
            System.out.println("Remove response: " + response.getCode());
            return null;
        });

        // Clear cart
        HttpPost clearPost = new HttpPost(baseUrl + "/clear");
        httpClient.execute(clearPost, response -> {
            System.out.println("Clear response: " + response.getCode());
            return null;
        });
    }

    private void addBook(String title) throws IOException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("title", title));
        HttpPost post = new HttpPost(baseUrl + "/add");
        post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        httpClient.execute(post, response -> {
            System.out.println("Added book: " + title + ", response: " + response.getCode());
            return null;
        });
    }
}
