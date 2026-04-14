package org.petclinic.controller;

import java.io.IOException;

import org.thymeleaf.context.WebContext;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/error")
public class ErrorServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext ctx = createWebContext(request, response);
        String message = (String) request.getAttribute("jakarta.servlet.error.message");
        if (message == null || message.isEmpty()) {
            message = "An unexpected error occurred";
        }
        ctx.setVariable("message", message);
        processTemplate("error", ctx, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
