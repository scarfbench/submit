package org.jakarta.samples.petclinic.system;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/oups")
public class CrashServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("message", "Expected: controller used to showcase what happens when an exception is thrown");
        req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
    }
}
