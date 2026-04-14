package spring.tutorial.web.dukeetf;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class DukeETFApplication implements QuarkusApplication {

    public static void main(String[] args) {
        Quarkus.run(DukeETFApplication.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        Quarkus.waitForExit();
        return 0;
    }
}
