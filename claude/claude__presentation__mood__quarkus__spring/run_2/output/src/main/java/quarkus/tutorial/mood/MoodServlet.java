package quarkus.tutorial.mood;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.io.PrintWriter;

@Controller
@RequestMapping("/report")
public class MoodServlet {

    @GetMapping
    @ResponseBody
    public String get(HttpServletRequest request) {
        return render(request);
    }

    @PostMapping
    @ResponseBody
    public String post(@RequestParam(value = "override", required = false) String override,
                       HttpServletRequest request) {
        if (override != null && !override.isBlank()) {
            request.setAttribute("mood", override.trim());
        }
        return render(request);
    }

    private String render(HttpServletRequest request) {
        final String contextPath = request.getContextPath();
        String mood = (String) request.getAttribute("mood");
        if (mood == null || mood.isBlank()) mood = "neutral";

        StringWriter sw = new StringWriter();
        try (PrintWriter out = new PrintWriter(sw)) {
            out.println("<html lang=\"en\">");
            out.println("<head>");
            out.println("<title>Servlet MoodServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet MoodServlet at " + contextPath + "</h1>");
            out.println("<p>Duke's mood is: " + mood + "</p>");
            switch (mood) {
                case "sleepy":
                    out.println("<img src=\"/images/duke.snooze.gif\" alt=\"Duke sleeping\"/><br/>");
                    break;
                case "alert":
                    out.println("<img src=\"/images/duke.waving.gif\" alt=\"Duke waving\"/><br/>");
                    break;
                case "hungry":
                    out.println("<img src=\"/images/duke.cookies.gif\" alt=\"Duke with cookies\"/><br/>");
                    break;
                case "lethargic":
                    out.println("<img src=\"/images/duke.handsOnHips.gif\" alt=\"Duke with hands on hips\"/><br/>");
                    break;
                case "thoughtful":
                    out.println("<img src=\"/images/duke.pensive.gif\" alt=\"Duke thinking\"/><br/>");
                    break;
                default:
                    out.println("<img src=\"/images/duke.thumbsup.gif\" alt=\"Duke with thumbs-up gesture\"/><br/>");
                    break;
            }
            out.println("</body>");
            out.println("</html>");
        }
        return sw.toString();
    }
}
