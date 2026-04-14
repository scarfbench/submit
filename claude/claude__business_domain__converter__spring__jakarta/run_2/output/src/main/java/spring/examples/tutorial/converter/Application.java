package spring.examples.tutorial.converter;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class Application {

	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/converter");
		server.setHandler(context);

		ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);
		jerseyServlet.setInitParameter("jersey.config.server.provider.packages",
			"spring.examples.tutorial.converter.controller");
		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",
			"spring.examples.tutorial.converter.service.ConverterService");

		try {
			server.start();
			System.out.println("Server started on port 8080 with context path /converter");
			server.join();
		} finally {
			server.destroy();
		}
	}

}
