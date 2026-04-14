package spring.examples.tutorial.cart;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jboss.weld.environment.servlet.Listener;

public class Application {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Enable Weld CDI
        context.addEventListener(new Listener());
        context.setInitParameter("org.jboss.weld.environment.servlet.archive.isolation", "false");

        // Configure Jersey
        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(1);
        jerseyServlet.setInitParameter("jakarta.ws.rs.Application",
            "spring.examples.tutorial.cart.config.JerseyConfig");

        server.start();
        System.out.println("Server started on http://localhost:8080");
        server.join();
    }
}
