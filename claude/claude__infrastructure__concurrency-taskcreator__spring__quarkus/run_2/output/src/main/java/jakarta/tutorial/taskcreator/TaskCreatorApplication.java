package jakarta.tutorial.taskcreator;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class TaskCreatorApplication {
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}