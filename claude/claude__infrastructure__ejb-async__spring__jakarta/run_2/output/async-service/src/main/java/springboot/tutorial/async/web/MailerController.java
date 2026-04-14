package springboot.tutorial.async.web;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.concurrent.Future;

import springboot.tutorial.async.ejb.MailerService;

/**
 * Servlet-based controller for Thymeleaf templates.
 * Handles email sending requests using Jakarta Servlet API.
 */
@WebServlet(name = "MailerController", urlPatterns = {"/thy", "/thy/index", "/thy/send", "/thy/response"})
public class MailerController extends HttpServlet {

    private static final String ATTR_FUTURE = "mailFuture";
    private static final String ATTR_STATUS = "mailStatus";

    @Inject
    private MailerService mailerService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        HttpSession session = request.getSession();

        if (path.equals("/thy") || path.equals("/thy/index")) {
            request.setAttribute("email", "");
            Object status = session.getAttribute(ATTR_STATUS);
            if (status != null) {
                request.setAttribute("status", status);
            }
            request.getRequestDispatcher("/templates/index.html").forward(request, response);
        } else if (path.equals("/thy/response")) {
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
            request.setAttribute("status", status);
            request.getRequestDispatcher("/templates/response.html").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if (path.equals("/thy/send")) {
            String email = request.getParameter("email");
            HttpSession session = request.getSession();
            Future<String> future = mailerService.sendMessage(email);
            session.setAttribute(ATTR_FUTURE, future);
            session.setAttribute(ATTR_STATUS, "Processing... (refresh to check again)");
            response.sendRedirect(request.getContextPath() + "/thy/response");
        }
    }
}
