package spring.tutorial.mood.web;

import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/report")
public class MoodController {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getReport(@Context HttpServerRequest request,
                            @QueryParam("name") @DefaultValue("") String name) {
        // In Quarkus, we get the mood attribute from the routing context
        String mood = request.getHeader("X-Mood");
        if (mood == null) {
            mood = "awake";
        }
        String person = name.isBlank() ? "friend" : name;

        // Produce the same HTML your servlet printed, including images now under /images/...
        return """
               <!doctype html>
               <html lang="en">
                 <head><meta charset="utf-8"><title>Mood Report</title></head>
                 <body>
                   <h1>Mood report</h1>
                   <p>Hello %s — current mood: <b>%s</b></p>
                   <img src="/images/duke.waving.gif" alt="duke waving">
                 </body>
               </html>
               """.formatted(person, mood);
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    public String postReport(@Context HttpServerRequest request,
                             @QueryParam("name") @DefaultValue("") String name) {
        // Reuse the GET logic (your original servlet had doPost→processRequest)
        return getReport(request, name);
    }
}
