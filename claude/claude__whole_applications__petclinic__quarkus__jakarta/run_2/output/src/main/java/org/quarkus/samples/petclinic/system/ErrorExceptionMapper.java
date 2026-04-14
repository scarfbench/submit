package org.quarkus.samples.petclinic.system;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global exception mapper for JAX-RS.
 * Replaces Quarkus @ServerExceptionMapper with standard Jakarta EE ExceptionMapper.
 */
@Provider
public class ErrorExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(ErrorExceptionMapper.class.getName());
    public static final String ERROR_HEADER = "x-error";

    @Inject
    TemplateEngine templateEngine;

    @Override
    public Response toResponse(Exception exception) {
        LOG.log(Level.SEVERE, "Internal application error", exception);
        Map<String, Object> vars = new HashMap<>();
        vars.put("message", exception.getMessage());
        String html = templateEngine.render("error", vars);
        return Response.ok(html).header(ERROR_HEADER, true).build();
    }
}
