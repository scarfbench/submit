package spring.tutorial.mood.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/report")
public class MoodController {

    @Inject
    MoodService moodService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getReport(@QueryParam("name") @DefaultValue("") String name) {
        String mood = moodService.getMood();
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
    public String postReport(@QueryParam("name") @DefaultValue("") String name) {
        // Reuse the GET logic (your original servlet had doPost→processRequest)
        return getReport(name);
    }
}
