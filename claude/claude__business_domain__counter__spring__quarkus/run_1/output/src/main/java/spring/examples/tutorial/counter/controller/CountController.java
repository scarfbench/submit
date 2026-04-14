package spring.examples.tutorial.counter.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import spring.examples.tutorial.counter.service.CounterService;

@Path("/")
public class CountController {

    @Inject
    CounterService counterService;

    @Inject
    Template index;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        int hitCount = counterService.getHits();
        return index.data("hitCount", hitCount);
    }
}
