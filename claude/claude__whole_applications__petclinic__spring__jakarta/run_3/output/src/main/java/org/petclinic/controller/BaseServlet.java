package org.petclinic.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class BaseServlet extends HttpServlet {

    @Inject
    protected ITemplateEngine templateEngine;

    protected WebContext createWebContext(HttpServletRequest request, HttpServletResponse response) {
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(request.getServletContext());
        IWebExchange exchange = application.buildExchange(request, response);
        Locale locale = request.getLocale() != null ? request.getLocale() : Locale.ENGLISH;
        WebContext ctx = new WebContext(exchange, locale);

        // Load messages for i18n
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages.messages", locale);
            for (String key : bundle.keySet()) {
                ctx.setVariable(key, bundle.getString(key));
            }
        } catch (Exception e) {
            // Fallback to English
            try {
                ResourceBundle bundle = ResourceBundle.getBundle("messages.messages", Locale.ENGLISH);
                for (String key : bundle.keySet()) {
                    ctx.setVariable(key, bundle.getString(key));
                }
            } catch (Exception ex) {
                // Ignore if no bundle found
            }
        }

        return ctx;
    }

    protected void processTemplate(String templateName, WebContext context,
                                   HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String result = templateEngine.process(templateName, context);
        response.getWriter().write(result);
    }

    protected String getParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value != null ? value.trim() : null;
    }

    protected String getParam(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }

    protected int getIntParam(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    protected LocalDate parseDateParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }

    protected int extractPathId(HttpServletRequest request, String prefix) {
        String path = request.getPathInfo();
        if (path == null) {
            path = request.getServletPath();
        }
        // Try to extract ID from path like /owners/1 or /owners/1/edit etc.
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals(prefix) && i + 1 < parts.length) {
                try {
                    return Integer.parseInt(parts[i + 1]);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    protected boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
