package spring.examples.tutorial.cart;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.io.IOException;
import java.net.URI;

public class Application {

    private static final String BASE_URI = "http://0.0.0.0:8080/";

    public static void main(String[] args) throws IOException, InterruptedException {
        // Initialize Weld CDI container
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();

        // Create Jersey resource configuration
        ResourceConfig config = new ResourceConfig()
                .packages("spring.examples.tutorial.cart")
                .register(new JerseyWeldBridge(container));

        // Create and start Grizzly HTTP server
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

        System.out.println(String.format("Jersey app started with endpoints available at %s", BASE_URI));
        System.out.println("Press Ctrl+C to stop the server...");

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.shutdownNow();
            weld.shutdown();
        }));

        // Keep the server running
        Thread.currentThread().join();
    }
}
