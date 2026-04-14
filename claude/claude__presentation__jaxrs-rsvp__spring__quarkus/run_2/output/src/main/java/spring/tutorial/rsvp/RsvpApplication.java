package spring.tutorial.rsvp;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class RsvpApplication {
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
