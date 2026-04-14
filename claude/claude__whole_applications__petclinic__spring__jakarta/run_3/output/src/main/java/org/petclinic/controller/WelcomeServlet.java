package org.petclinic.controller;

import java.io.IOException;

import org.thymeleaf.context.WebContext;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"", "/"})
public class WelcomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext ctx = createWebContext(request, response);
        processTemplate("welcome", ctx, response);
    }
}
