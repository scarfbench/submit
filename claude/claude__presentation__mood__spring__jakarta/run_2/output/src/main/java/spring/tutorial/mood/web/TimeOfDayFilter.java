package spring.tutorial.mood.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class TimeOfDayFilter implements Filter {

    // Provide a default; can be overridden via init-param if desired
    private String mood = "awake";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String moodParam = filterConfig.getInitParameter("mood");
        if (moodParam != null && !moodParam.isBlank()) {
            this.mood = moodParam;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            // Example: set an attribute based on time/mood (same behavior you had)
            httpRequest.setAttribute("mood", mood);
        }
        // ... any time-of-day logic you previously had ...
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
