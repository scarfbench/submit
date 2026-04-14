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
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import java.net.URI;
import java.util.concurrent.Future;

import springboot.tutorial.async.ejb.MailerService;

@Path("/thy")
public class MailerController {

    private static final String ATTR_FUTURE = "mailFuture";
    private static final String ATTR_STATUS = "mailStatus";

    @Inject
    MailerService mailerService;

    @Inject
    Template index;

    @Inject
    Template response;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance form(@Context HttpSession session) {
        Object status = session.getAttribute(ATTR_STATUS);
        return index.data("email", "")
                    .data("status", status);
    }

    @GET
    @Path("/index")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance formIndex(@Context HttpSession session) {
        return form(session);
    }

    @POST
    @Path("/send")
    public Response send(@FormParam("email") String email, @Context HttpSession session) {
        Future<String> future = mailerService.sendMessage(email);
        session.setAttribute(ATTR_FUTURE, future);
        session.setAttribute(ATTR_STATUS, "Processing... (refresh to check again)");
        return Response.seeOther(URI.create("/thy/response")).build();
    }

    @GET
    @Path("/response")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance responseView(@Context HttpSession session) {
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
        return response.data("status", status);
    }
}
