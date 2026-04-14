package jakarta.examples.tutorial.counter.controller;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.examples.tutorial.counter.service.CounterService;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.util.Locale;

/**
 * Jakarta Servlet for handling counter page requests
 */
@WebServlet(urlPatterns = {"/", "/index"}, loadOnStartup = 1)
public class CountController extends HttpServlet {

    @Inject
    private CounterService counterService;

    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize Thymeleaf template engine
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        // Get hit count from service
        int hitCount = counterService.getHits();

        // Create Thymeleaf context
        Context context = new Context(Locale.getDefault());

        // Add attributes to context
        context.setVariable("hitCount", hitCount);

        // Process template
        templateEngine.process("index", context, response.getWriter());
    }
}
