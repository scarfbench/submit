package jakarta.tutorial.mood.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(filterName = "TimeOfDayFilter", urlPatterns = {"/*"})
public class TimeOfDayFilter implements Filter {

    private String mood;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Provide a default; can be overridden via filter init parameter
        this.mood = filterConfig.getInitParameter("mood");
        if (this.mood == null || this.mood.isEmpty()) {
            this.mood = "awake";
        }
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request,
                        jakarta.servlet.ServletResponse response,
                        FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Set mood attribute based on time of day or default
            httpRequest.setAttribute("mood", mood);

            chain.doFilter(httpRequest, httpResponse);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
