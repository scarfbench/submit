package spring.examples.tutorial.helloservice;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

public class JakartaApplication extends Application {

	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/helloservice");
		server.setHandler(context);

		ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);
		jerseyServlet.setInitParameter("jersey.config.server.provider.packages",
			"spring.examples.tutorial.helloservice.controller");

		try {
			server.start();
			server.join();
		} finally {
			server.destroy();
		}
	}
}
