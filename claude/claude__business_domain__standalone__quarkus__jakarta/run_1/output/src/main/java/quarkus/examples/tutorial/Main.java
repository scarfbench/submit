package quarkus.examples.tutorial;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        logger.info("Starting Jakarta CDI application");

        try (WeldContainer container = new Weld().initialize()) {
            StandaloneBean bean = container.select(StandaloneBean.class).get();
            String message = bean.returnMessage();
            logger.info("Message from StandaloneBean: " + message);
        }

        logger.info("Application completed successfully");
    }
}
