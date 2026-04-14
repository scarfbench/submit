package org.quarkus.samples.petclinic.system;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;

@Path("/")
public class WelcomeResource {

    @Inject
    TemplateEngine templateEngine;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        return templateEngine.render("welcome", new HashMap<>());
    }
}
