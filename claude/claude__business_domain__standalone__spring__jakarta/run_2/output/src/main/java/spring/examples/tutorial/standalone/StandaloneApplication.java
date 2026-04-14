package spring.examples.tutorial.standalone;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import spring.examples.tutorial.standalone.service.StandaloneService;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

public class StandaloneApplication {

	public static void main(String[] args) {
		// Initialize Jakarta EE CDI container using Weld SE
		try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
			// Get the StandaloneService bean from the CDI container
			StandaloneService service = container.select(StandaloneService.class).get();

			// Execute the service to demonstrate it's working
			String message = service.returnMessage();
			System.out.println("Application started successfully!");
			System.out.println("Service message: " + message);
		}
	}

}
