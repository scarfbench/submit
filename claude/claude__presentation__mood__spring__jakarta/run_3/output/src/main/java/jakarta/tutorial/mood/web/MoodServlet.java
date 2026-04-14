package jakarta.tutorial.mood.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "MoodServlet", urlPatterns = {"/report"})
public class MoodServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");

        String mood = (String) request.getAttribute("mood");
        String name = request.getParameter("name");
        String person = (name == null || name.isBlank()) ? "friend" : name;

        try (PrintWriter out = response.getWriter()) {
            out.println("<!doctype html>");
            out.println("<html lang=\"en\">");
            out.println("<head>");
            out.println("<meta charset=\"utf-8\">");
            out.println("<title>Mood Report</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Mood report</h1>");
            out.println("<p>Hello " + person + " — current mood: <b>" + mood + "</b></p>");
            out.println("<img src=\"/mood/images/duke.waving.gif\" alt=\"duke waving\">");
            out.println("</body>");
            out.println("</html>");
        }
    }
}
