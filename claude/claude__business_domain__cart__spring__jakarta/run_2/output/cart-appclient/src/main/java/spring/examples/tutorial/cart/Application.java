package spring.examples.tutorial.cart;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class Application {

    public static void main(String[] args) {
        // Initialize Weld CDI container
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();

        try {
            // Get CartClient bean from CDI container
            CartClient cartClient = container.select(CartClient.class).get();
            cartClient.doCartOperations();
        } catch (Exception ex) {
            System.err.println("Caught a BookException: " + ex.getMessage());
        } finally {
            weld.shutdown();
        }
    }

}
