package springboot.tutorial.async;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class AsyncApplication {
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
