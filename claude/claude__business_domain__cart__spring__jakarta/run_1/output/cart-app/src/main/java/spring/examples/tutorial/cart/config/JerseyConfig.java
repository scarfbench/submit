package spring.examples.tutorial.cart.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import spring.examples.tutorial.cart.CartController;

@ApplicationPath("/")
public class JerseyConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        resources.add(CartController.class);
        return resources;
    }
}
