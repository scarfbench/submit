package io.quarkuscoffeeshop.coffeeshop.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Path("/")
public class WebResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebResource.class);

    private String streamUrl = System.getenv().getOrDefault("STREAM_URL", "http://localhost:8080/dashboard/stream");
    private String storeId = System.getenv().getOrDefault("STORE_ID", "ATLANTA");

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getIndex() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/resources/coffeeshopTemplate.html");
            if (is == null) {
                return "<html><body><h1>Coffee Shop</h1><p>Template not found</p></body></html>";
            }
            String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // Replace Qute-style placeholders
            template = template.replace("{streamUrl}", streamUrl);
            template = template.replace("{storeId}", storeId);
            return template;
        } catch (Exception e) {
            LOGGER.error("Error loading template", e);
            return "<html><body><h1>Coffee Shop</h1><p>Error loading page</p></body></html>";
        }
    }
}
