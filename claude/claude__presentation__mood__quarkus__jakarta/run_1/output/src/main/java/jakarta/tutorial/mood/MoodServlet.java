package jakarta.tutorial.mood;

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
        response.setContentType("text/html;charset=UTF-8");
        render(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String override = request.getParameter("override");
        if (override != null && !override.isBlank()) {
            request.setAttribute("mood", override.trim());
        }
        response.setContentType("text/html;charset=UTF-8");
        render(request, response);
    }

    private void render(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        final String contextPath = request.getContextPath();
        String mood = (String) request.getAttribute("mood");
        if (mood == null || mood.isBlank()) {
            mood = "neutral";
        }

        try (PrintWriter out = response.getWriter()) {
            out.println("<html lang=\"en\">");
            out.println("<head>");
            out.println("<title>Servlet MoodServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet MoodServlet at " + contextPath + "</h1>");
            out.println("<p>Duke's mood is: " + mood + "</p>");
            switch (mood) {
                case "sleepy":
                    out.println("<img src=\"" + contextPath + "/images/duke.snooze.gif\" alt=\"Duke sleeping\"/><br/>");
                    break;
                case "alert":
                    out.println("<img src=\"" + contextPath + "/images/duke.waving.gif\" alt=\"Duke waving\"/><br/>");
                    break;
                case "hungry":
                    out.println("<img src=\"" + contextPath + "/images/duke.cookies.gif\" alt=\"Duke with cookies\"/><br/>");
                    break;
                case "lethargic":
                    out.println("<img src=\"" + contextPath + "/images/duke.handsOnHips.gif\" alt=\"Duke with hands on hips\"/><br/>");
                    break;
                case "thoughtful":
                    out.println("<img src=\"" + contextPath + "/images/duke.pensive.gif\" alt=\"Duke thinking\"/><br/>");
                    break;
                default:
                    out.println("<img src=\"" + contextPath + "/images/duke.thumbsup.gif\" alt=\"Duke with thumbs-up gesture\"/><br/>");
                    break;
            }
            out.println("</body>");
            out.println("</html>");
        }
    }
}
