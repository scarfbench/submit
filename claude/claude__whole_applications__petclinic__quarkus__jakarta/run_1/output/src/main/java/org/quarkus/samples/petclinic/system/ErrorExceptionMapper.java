package org.quarkus.samples.petclinic.system;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ErrorExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(ErrorExceptionMapper.class);
    public static final String ERROR_HEADER = "x-error";

    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("Internal application error", exception);

        // Build an HTML error page inline (since we can't forward from ExceptionMapper)
        String html = buildErrorPage(exception.getMessage());
        return Response.ok(html, "text/html")
                .header(ERROR_HEADER, "true")
                .build();
    }

    private String buildErrorPage(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html><head>");
        sb.append("<meta charset='utf-8'>");
        sb.append("<title>PetClinic :: Error</title>");
        sb.append("<link rel='stylesheet' href='");
        String ctx = request != null ? request.getContextPath() : "";
        sb.append(ctx);
        sb.append("/webjars/font-awesome/4.7.0/css/font-awesome.min.css'>");
        sb.append("<link rel='stylesheet' href='").append(ctx).append("/css/petclinic.css' />");
        sb.append("</head><body>");
        sb.append("<nav class='navbar navbar-expand-lg navbar-dark' role='navigation'><div class='container'>");
        sb.append("<div class='navbar-header'><a class='navbar-brand' href='").append(ctx).append("/app/'><span></span></a></div>");
        sb.append("<div class='collapse navbar-collapse' id='main-navbar'><ul class='nav navbar-nav me-auto'>");
        sb.append("<li class='active'><a href='").append(ctx).append("/app/'><span class='fa fa-home'></span> Home</a></li>");
        sb.append("<li><a href='").append(ctx).append("/app/owners/find'><span class='fa fa-search'></span> Find Owners</a></li>");
        sb.append("<li><a href='").append(ctx).append("/app/vets.html'><span class='fa fa-th-list'></span> Veterinarians</a></li>");
        sb.append("</ul></div></div></nav>");
        sb.append("<div class='container-fluid'><div class='container xd-container'>");
        sb.append("<h2>Welcome</h2><div class='row'><div class='col-md-12'>");
        sb.append("<img class='img-responsive' src='").append(ctx).append("/images/pets.png'/>");
        sb.append("<h2>Error</h2><p>Something happened...</p>");
        if (message != null) {
            sb.append("<p>").append(escapeHtml(message)).append("</p>");
        }
        sb.append("</div></div></div></div>");
        sb.append("<script src='").append(ctx).append("/webjars/bootstrap/5.1.3/dist/js/bootstrap.bundle.min.js'></script>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
