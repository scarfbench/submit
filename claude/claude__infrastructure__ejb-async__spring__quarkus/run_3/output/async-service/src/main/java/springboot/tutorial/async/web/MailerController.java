package springboot.tutorial.async.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.concurrent.Future;

import springboot.tutorial.async.ejb.MailerService;

@Path("/thy")
@ApplicationScoped
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
    public TemplateInstance form(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object status = session.getAttribute(ATTR_STATUS);
        return index.data("email", "").data("status", status != null ? status : "");
    }

    @GET
    @Path("/index")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance formIndex(@Context HttpServletRequest request) {
        return form(request);
    }

    @POST
    @Path("/send")
    @Produces(MediaType.TEXT_HTML)
    public Response send(@FormParam("email") String email, @Context HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Future<String> future = mailerService.sendMessage(email);
        session.setAttribute(ATTR_FUTURE, future);
        session.setAttribute(ATTR_STATUS, "Processing... (refresh to check again)");
        return Response.seeOther(URI.create("/thy/response")).build();
    }

    @GET
    @Path("/response")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance responseView(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(true);
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
        return response.data("status", status != null ? status : "");
    }
}
