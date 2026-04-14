package springboot.tutorial.async.web;

import jakarta.servlet.http.HttpSession;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.Future;
import java.net.URI;

import springboot.tutorial.async.ejb.MailerService;

@Path("/thy")
public class MailerController {

    private static final String ATTR_FUTURE = "mailFuture";
    private static final String ATTR_STATUS = "mailStatus";

    @Inject
    private MailerService mailerService;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response form(@Context HttpSession session) {
        // Redirect to JSF page
        return Response.seeOther(URI.create("/index.xhtml")).build();
    }

    @GET
    @Path("/index")
    @Produces(MediaType.TEXT_HTML)
    public Response index(@Context HttpSession session) {
        // Redirect to JSF page
        return Response.seeOther(URI.create("/index.xhtml")).build();
    }

    @POST
    @Path("/send")
    @Produces(MediaType.TEXT_HTML)
    public Response send(@FormParam("email") String email, @Context HttpSession session) {
        Future<String> future = mailerService.sendMessage(email);
        session.setAttribute(ATTR_FUTURE, future);
        session.setAttribute(ATTR_STATUS, "Processing... (refresh to check again)");
        return Response.seeOther(URI.create("/thy/response")).build();
    }

    @GET
    @Path("/response")
    @Produces(MediaType.TEXT_HTML)
    public Response response(@Context HttpSession session) {
        @SuppressWarnings("unchecked")
        Future<String> future = (Future<String>) session.getAttribute(ATTR_FUTURE);
        String status = (String) session.getAttribute(ATTR_STATUS);
        if (future != null && future.isDone()) {
            try {
                status = future.get();
            } catch (Exception e) {
                status = e.getMessage();
            }
            session.setAttribute(ATTR_STATUS, status);
        }
        // Redirect to JSF response page
        return Response.seeOther(URI.create("/response.xhtml")).build();
    }
}
