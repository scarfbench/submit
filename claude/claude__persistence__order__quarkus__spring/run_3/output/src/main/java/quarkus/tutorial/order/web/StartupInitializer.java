package quarkus.tutorial.order.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;
import quarkus.tutorial.order.service.OrderConfigService;

@Component
public class StartupInitializer implements CommandLineRunner {
    private static final Logger logger = Logger.getLogger(StartupInitializer.class.getName());

    @Autowired
    private OrderConfigService orderConfigService;

    @Override
    public void run(String... args) {
        logger.info("Manually triggering dataset initialization");
        orderConfigService.createData();
        logger.info("Manual dataset initialization completed");
    }
}
