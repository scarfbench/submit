package spring.tutorial.mood.web;

import jakarta.servlet.ServletContextAttributeEvent;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.jboss.logging.Logger;

@WebListener
public class SimpleServletListener implements ServletContextListener, ServletContextAttributeListener {
    private static final Logger log = Logger.getLogger(SimpleServletListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Context initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Context destroyed");
    }

    @Override
    public void attributeAdded(ServletContextAttributeEvent event) {
        log.infof("Attribute added: %s=%s", event.getName(), event.getValue());
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent event) {
        log.infof("Attribute removed: %s", event.getName());
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent event) {
        log.infof("Attribute replaced: %s", event.getName());
    }
}
