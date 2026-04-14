package jakarta.tutorial.mood.web;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/report")
public class MoodController {

    @Context
    private HttpServletRequest request;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getReport(@QueryParam("name") @DefaultValue("") String name) {
        String mood = (String) request.getAttribute("mood");
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
