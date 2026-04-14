package spring.examples.tutorial.standalone;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import spring.examples.tutorial.standalone.service.StandaloneService;

public class StandaloneApplication {

	public static void main(String[] args) {
		// Initialize CDI container for Jakarta EE standalone application
		SeContainerInitializer initializer = SeContainerInitializer.newInstance();
		try (SeContainer container = initializer.initialize()) {
			// Get the service from CDI container
			StandaloneService service = container.select(StandaloneService.class).get();

			// Execute the service method
			String message = service.returnMessage();
			System.out.println("Application started successfully. Message: " + message);
		}
	}

}
