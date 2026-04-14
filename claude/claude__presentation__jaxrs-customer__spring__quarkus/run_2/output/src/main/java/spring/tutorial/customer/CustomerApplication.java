package spring.tutorial.customer;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.context.ApplicationScoped;

@QuarkusMain
@ApplicationScoped
public class CustomerApplication implements QuarkusApplication {

    public static void main(String[] args) {
        Quarkus.run(CustomerApplication.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        Quarkus.waitForExit();
        return 0;
    }
}
