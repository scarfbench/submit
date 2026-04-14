package spring.tutorial.order.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import spring.tutorial.order.service.OrderConfigService;
import java.util.logging.Logger;

@Component
public class StartupInitializer {
    private static final Logger logger = Logger.getLogger(StartupInitializer.class.getName());

    @Autowired
    private OrderConfigService orderConfigService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        logger.info("Manually triggering dataset initialization");
        orderConfigService.createData();
        logger.info("Manual dataset initialization completed");
    }
}
