package spring.tutorial.web.dukeetf2;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class DukeEtfApplication implements QuarkusApplication {

    public static void main(String[] args) {
        Quarkus.run(DukeEtfApplication.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        Quarkus.waitForExit();
        return 0;
    }
}
