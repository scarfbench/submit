package spring.examples.tutorial.cart;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.servlet.Listener;

public class Application {

    public static void main(String[] args) throws Exception {
        // Initialize Weld CDI container
        Weld weld = new Weld();

        // Create Jetty server
        Server server = new Server(8080);

        // Create servlet context handler
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Add session handler
        SessionHandler sessionHandler = new SessionHandler();
        context.setSessionHandler(sessionHandler);

        // Add Weld listener for CDI
        context.addEventListener(new Listener());

        // Configure Jersey servlet
        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(1);
        jerseyServlet.setInitParameter("jersey.config.server.provider.packages",
                "spring.examples.tutorial.cart");
        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",
                "org.glassfish.jersey.media.multipart.MultiPartFeature");

        server.setHandler(context);

        try {
            server.start();
            System.out.println("Server started on port 8080");
            server.join();
        } finally {
            server.destroy();
        }
    }

}
