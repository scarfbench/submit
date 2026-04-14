package org.quarkus.samples.petclinic.system;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

/**
 * CDI-managed Thymeleaf template engine.
 * Replaces Quarkus Qute templating with standard Thymeleaf.
 * Each template renders a body fragment, which is wrapped in the base layout.
 */
@ApplicationScoped
public class TemplateEngine {

    private org.thymeleaf.TemplateEngine engine;

    private static final String BASE_HEADER = """
            <!doctype html>
            <html>
            <head>
              <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
              <meta charset="utf-8">
              <meta http-equiv="X-UA-Compatible" content="IE=edge">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>PetClinic :: a Jakarta EE demonstration</title>
              <link rel="stylesheet" href="/petclinic/webjars/font-awesome/4.7.0/css/font-awesome.min.css">
              <link rel="stylesheet" href="/petclinic/resources/css/petclinic.css" />
            </head>
            <body>
              <nav class="navbar navbar-expand-lg navbar-dark" role="navigation">
                <div class="container">
                  <div class="navbar-header">
                    <a class="navbar-brand" href="/petclinic/"><span></span></a>
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#main-navbar">
                      <span class="navbar-toggler-icon"></span>
                    </button>
                  </div>
                  <div class="collapse navbar-collapse" id="main-navbar">
                    <ul class="nav navbar-nav me-auto">
                      <li class="active">
                        <a href="/petclinic/" title="home page">
                          <span class="fa fa-home" aria-hidden="true"></span>
                          <span>Home</span>
                        </a>
                      </li>
                      <li>
                        <a href="/petclinic/owners/find" title="find owners">
                          <span class="fa fa-search" aria-hidden="true"></span>
                          <span>Find Owners</span>
                        </a>
                      </li>
                      <li>
                        <a href="/petclinic/vets.html" title="veterinarians">
                          <span class="fa fa-th-list" aria-hidden="true"></span>
                          <span>Veterinarians</span>
                        </a>
                      </li>
                      <li>
                        <a href="/petclinic/oups" title="trigger a RuntimeException to see how it is handled">
                          <span class="fa exclamation-triangle" aria-hidden="true"></span>
                          <span>Error</span>
                        </a>
                      </li>
                    </ul>
                  </div>
                </div>
              </nav>
              <div class="container-fluid">
                <div class="container xd-container">
            """;

    private static final String BASE_FOOTER = """
                </div>
              </div>
              <script src="/petclinic/webjars/bootstrap/5.1.3/dist/js/bootstrap.bundle.min.js"></script>
            </body>
            </html>
            """;

    @PostConstruct
    public void init() {
        engine = new org.thymeleaf.TemplateEngine();
        org.thymeleaf.templateresolver.ClassLoaderTemplateResolver resolver =
                new org.thymeleaf.templateresolver.ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(org.thymeleaf.templatemode.TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);
        engine.setTemplateResolver(resolver);
    }

    public String render(String templateName, Map<String, Object> variables) {
        Context ctx = new Context(Locale.getDefault());
        // Add all message keys as variables
        Map<String, String> messages = AppMessages.getMessages();
        ctx.setVariable("msg", messages);
        if (variables != null) {
            variables.forEach(ctx::setVariable);
        }
        String body = engine.process(templateName, ctx);
        return BASE_HEADER + body + BASE_FOOTER;
    }
}
