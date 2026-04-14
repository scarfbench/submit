package jakarta.tutorial.order.web;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ThymeleafEngine {

    private static final Logger logger = Logger.getLogger(ThymeleafEngine.class.getName());
    private TemplateEngine engine;

    @PostConstruct
    public void init() {
        engine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        engine.setTemplateResolver(resolver);
        logger.info("ThymeleafEngine initialized");
    }

    public String render(String templateName, Map<String, Object> variables) {
        try {
            Context ctx = new Context();
            if (variables != null) {
                ctx.setVariables(variables);
            }
            return engine.process(templateName, ctx);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error rendering template: " + templateName, e);
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            System.err.println("THYMELEAF ERROR: " + sw.toString());
            throw e;
        }
    }
}
