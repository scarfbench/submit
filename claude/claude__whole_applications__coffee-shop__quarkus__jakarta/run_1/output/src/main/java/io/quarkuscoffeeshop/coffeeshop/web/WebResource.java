package io.quarkuscoffeeshop.coffeeshop.web;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;

@Path("/")
public class WebResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getIndex() {
        InputStream html = getClass().getResourceAsStream("/META-INF/resources/index.html");
        if (html != null) {
            return Response.ok(html).type(MediaType.TEXT_HTML).build();
        }
        return Response.ok("<html><body><h1>Coffeeshop - Jakarta EE</h1><p>Welcome to the Coffee Shop!</p></body></html>")
                .type(MediaType.TEXT_HTML).build();
    }
}
