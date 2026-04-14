package spring.tutorial.mood;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class MoodApplication {
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
