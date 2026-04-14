package jakarta.tutorial.order.web;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import java.util.logging.Logger;
import jakarta.tutorial.order.service.OrderConfigService;

@Singleton
@Startup
public class StartupInitializer {
    private static final Logger logger = Logger.getLogger(StartupInitializer.class.getName());

    @Inject
    OrderConfigService orderConfigService;

    @PostConstruct
    public void init() {
        logger.info("Manually triggering dataset initialization");
        orderConfigService.createData();
        logger.info("Manual dataset initialization completed");
    }
}
