package spring.examples.tutorial.counter.controller;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import spring.examples.tutorial.counter.service.CounterService;

import java.io.IOException;

@WebServlet(name = "CountController", urlPatterns = {"/count"})
public class CountController extends HttpServlet {

    @Inject
    private CounterService counterService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int hitCount = counterService.getHits();
        request.setAttribute("hitCount", hitCount);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
