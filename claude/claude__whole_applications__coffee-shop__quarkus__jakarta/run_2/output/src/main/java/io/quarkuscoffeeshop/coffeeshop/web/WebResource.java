package io.quarkuscoffeeshop.coffeeshop.web;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Path("/")
public class WebResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebResource.class);

    @Inject
    @ConfigProperty(name = "streamUrl", defaultValue = "http://localhost:9080/dashboard/stream")
    String streamUrl;

    @Inject
    @ConfigProperty(name = "storeId", defaultValue = "ATLANTA")
    String storeId;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getIndex() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("templates/coffeeshopTemplate.html")) {
            if (is == null) {
                return "<html><body><h1>Coffee Shop</h1><p>Template not found</p></body></html>";
            }
            String template = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            // Replace Qute template placeholders with actual values
            template = template.replace("{storeId}", storeId);
            template = template.replace("{streamUrl}", streamUrl);
            return template;
        } catch (Exception e) {
            LOGGER.error("Error loading template", e);
            return "<html><body><h1>Coffee Shop</h1><p>Error loading page</p></body></html>";
        }
    }
}
