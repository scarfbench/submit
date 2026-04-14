package quarkus.examples.tutorial;

import org.springframework.stereotype.Component;

@Component
public class StandaloneBean {

    private static final String message = "Greetings!";

    public String returnMessage() {
        return message;
    }

}
