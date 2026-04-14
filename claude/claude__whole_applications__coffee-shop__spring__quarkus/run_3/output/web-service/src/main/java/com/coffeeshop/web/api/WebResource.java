package com.coffeeshop.web.api;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class WebResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebResource.class);

    @Inject
    Template coffeeshop;

    @ConfigProperty(name = "streamUrl")
    String streamUrl;

    @ConfigProperty(name = "storeId")
    String storeId;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getIndex() {
        LOGGER.debug("Rendering coffeeshop page with streamUrl={} storeId={}", streamUrl, storeId);
        return coffeeshop
            .data("streamUrl", streamUrl)
            .data("storeId", storeId);
    }
}
