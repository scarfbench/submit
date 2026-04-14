package spring.examples.tutorial.standalone;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class StandaloneApplication {

	public static void main(String[] args) {
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			System.out.println("Jakarta EE CDI container initialized successfully");
			// Keep the application running
			container.select(StandaloneApplication.class).get();
		}
	}

}
