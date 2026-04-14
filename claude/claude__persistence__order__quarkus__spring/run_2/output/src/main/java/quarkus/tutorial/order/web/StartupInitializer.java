package quarkus.tutorial.order.web;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.logging.Logger;
import quarkus.tutorial.order.service.OrderConfigService;

@Component
public class StartupInitializer implements ApplicationRunner {
    private static final Logger logger = Logger.getLogger(StartupInitializer.class.getName());

    @Autowired
    OrderConfigService orderConfigService;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Manually triggering dataset initialization");
        orderConfigService.createData();
        logger.info("Manual dataset initialization completed");
    }
}
