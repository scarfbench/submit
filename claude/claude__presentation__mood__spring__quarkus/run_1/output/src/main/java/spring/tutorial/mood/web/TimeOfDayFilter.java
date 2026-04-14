package spring.tutorial.mood.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@WebFilter("/*")
public class TimeOfDayFilter implements Filter {

    // Provide a default; can be overridden via FilterRegistrationBean if desired
    private String mood;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.mood = "awake";
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
        // cleanup if needed
    }
}
