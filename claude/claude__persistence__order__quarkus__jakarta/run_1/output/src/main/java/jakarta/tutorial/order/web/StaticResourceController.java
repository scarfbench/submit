package jakarta.tutorial.order.web;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;

@Path("/css")
public class StaticResourceController {

    @GET
    @Path("/{filename}")
    @Produces("text/css")
    public Response getCss(@PathParam("filename") String filename) {
        // Sanitize filename to prevent path traversal
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("css/" + filename);
        if (is == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(is).type("text/css").build();
    }
}
